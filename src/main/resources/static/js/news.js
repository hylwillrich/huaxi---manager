$(document).ready(function() {
    // 日期格式化函数
    function formatDate(dateStr) {
        if (!dateStr) return '';
        const date = new Date(dateStr);
        return date.toLocaleDateString('zh-CN', {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit'
        });
    }

    let currentPage = 1;
    const pageSize = 10;
    
    // 初始化页面
    loadNews(currentPage);
    
    // 添加新闻按钮点击事件
    $('#addBtn').click(function() {
        $('#modalTitle').text('添加新闻');
        $('#newsForm')[0].reset();
        $('#n_id').val(''); // 确保ID为空
        $('#imagePreview').empty();
        // 设置默认日期为今天
        const today = new Date().toISOString().split('T')[0];
        $('#n_date').val(today);
        $('#newsModal').modal('show');
    });
    
    // 图片选择变化事件
    $('#n_pic').change(function(e) {
        const file = e.target.files[0];
        if (!file) return;
        
        // 验证图片
        if (!validateImage(file)) {
            $(this).val('');
            return;
        }
        
        previewImage(file);
    });
    
    // 验证图片文件
    function validateImage(file) {
        const validTypes = ['image/jpeg', 'image/png', 'image/gif'];
        if (!validTypes.includes(file.type)) {
            showToast('请选择有效的图片格式 (JPEG, PNG, GIF)', 'danger');
            return false;
        }
        
        if (file.size > 10 * 1024 * 1024) {
            showToast('图片大小不能超过10MB', 'danger');
            return false;
        }
        
        return true;
    }
    
    // 预览图片
    function previewImage(file) {
        const reader = new FileReader();
        reader.onload = function(event) {
            $('#imagePreview').html(`<img src="${event.target.result}" class="img-thumbnail" alt="预览">`);
        };
        reader.readAsDataURL(file);
    }
    
    // 保存新闻
    $('#saveBtn').click(function() {
        const formData = new FormData();
        const newsId = $('#n_id').val();
        const isEdit = !!newsId;
        
        // 表单验证
        if (!validateForm()) return;
        
        // 准备表单数据
        prepareFormData(formData, newsId, isEdit);
        
        // 发送请求
        const url = isEdit ? '/news/update' : '/news/add';
        $.ajax({
            url: url,
            type: 'POST',
            data: formData,
            processData: false,
            contentType: false,
            success: function(response) {
                if (response.success) {
                    showToast(isEdit ? '更新成功' : '添加成功', 'success');
                    $('#newsModal').modal('hide');
                    // 添加0.5秒延迟确保图片上传完成
                    setTimeout(() => {
                        loadNews(currentPage);
                    }, 500);
                } else {
                    showToast(response.message || '操作失败', 'danger');
                }
            },
            error: function(xhr) {
                let errorMsg = '请求失败';
                try {
                    const response = JSON.parse(xhr.responseText);
                    errorMsg = response.message || errorMsg;
                } catch (e) {}
                showToast(errorMsg, 'danger');
            }
        });
    });
    
    // 验证表单
    function validateForm() {
        const title = $('#n_title').val().trim();
        const content = $('#n_content').val().trim();
        
        if (!title || title.length < 5) {
            showToast('标题不能少于5个字符', 'danger');
            return false;
        }
        
        if (!content || content.length < 10) {
            showToast('内容不能少于10个字符', 'danger');
            return false;
        }
        
        return true;
    }
    
    // 准备表单数据
    function prepareFormData(formData, newsId, isEdit) {
        formData.append('n_title', $('#n_title').val().trim());
        formData.append('n_content', $('#n_content').val().trim());
        
        // 处理日期 - 直接传递Date对象
        const dateInput = $('#n_date')[0];
        if (dateInput && dateInput.value) {
            const date = new Date(dateInput.value);
            formData.append('n_date', date.toISOString());
        }
        
        // 处理图片
        const photoFile = $('#n_pic')[0].files[0];
        if (photoFile) {
            formData.append('n_pic', photoFile);
        } else if (isEdit) {
            // 如果没有上传新图片，确保传递当前图片路径
            const currentPhotoPath = $('#currentPhotoPath').val();
            if (currentPhotoPath) {
                formData.append('currentPhotoName', currentPhotoPath.split('/').pop());
            }
        }
        
        // 编辑模式下添加ID
        if (isEdit) {
            formData.append('n_id', newsId);
        }
    }
    
    // 发送表单请求
    function sendFormRequest(url, formData, isEdit) {
        $.ajax({
            url: url,
            type: 'POST',
            data: formData,
            processData: false,
            contentType: false,
            success: function(response) {
                handleSaveResponse(response, isEdit);
            },
            error: handleAjaxError
        });
    }
    
    // 处理保存响应
    function handleSaveResponse(response, isEdit) {
                        if (response.success) {
                            showToast(isEdit ? '更新成功' : '添加成功', 'success');
                            $('#newsModal').modal('hide');
                            // 添加0.5秒延迟确保图片上传完成
                            setTimeout(() => {
                                loadNews(currentPage);
                            }, 1000);
                        } else {
                            showToast(response.message || '操作失败', 'danger');
                        }
    }
    
    // 加载新闻数据
    function loadNews(page) {
        $.get(`/news/page?page=${page}&size=${pageSize}`, function(data) {
            renderNewsTable(data.news);
            renderPagination(Math.ceil(data.total / pageSize), page);
        }).fail(function() {
            showToast('加载新闻失败', 'danger');
        });
    }
    
    // 绑定行事件
    function bindRowEvents($row) {
        $row.find('.edit-btn').click(function() {
            
            const newsId = $(this).data('id');
            if (!newsId) {
                console.error('编辑按钮缺少data-id属性');
                return;
            }
            editNews(newsId);
        });
        
        $row.find('.delete-btn').click(function() {
            const newsId = $(this).data('id');
            deleteNews(newsId);
        });
        
        $row.find('img').click(function() {
            showImage($(this).attr('src'));
        });
    }

    // 渲染新闻表格
    function renderNewsTable(newsList) {
        const $tbody = $('#newsTable tbody');
        $tbody.empty();
        
        if (!newsList || newsList.length === 0) {
            $tbody.append('<tr><td colspan="5" class="text-center">暂无新闻</td></tr>');
            return;
        }
        
        newsList.forEach(news => {
            const imgCell = news.n_pic 
                ? `<img src="${news.n_pic.startsWith('/') ? news.n_pic : `/images/news/${news.n_pic}`}" 
                     class="img-thumbnail" style="max-height: 50px;" alt="新闻图片">`
                : '<span class="text-muted">无图片</span>';
            const row = `
                <tr>
                    <td>${news.n_id}</td>
                    <td>${news.n_title}</td>
                    <td>${formatDate(news.n_date)}</td>
                    <td>${imgCell}</td>
                    <td>
                        <button class="btn btn-sm btn-primary edit-btn" data-id="${news.n_id}">编辑</button>
                        <button class="btn btn-sm btn-danger delete-btn" data-id="${news.n_id}">删除</button>
                    </td>
                </tr>
            `;
            const $row = $(row);
            $tbody.append($row);
            bindRowEvents($row);
        });
    }
    
    // 渲染分页
    function renderPagination(totalPages, currentPage) {
        const $pagination = $('#pagination');
        $pagination.empty();
        
        // 上一页按钮
        $pagination.append(`
            <li class="page-item ${currentPage === 1 ? 'disabled' : ''}">
                <a class="page-link" href="#" data-page="${currentPage - 1}">上一页</a>
            </li>
        `);
        
        // 页码按钮
        for (let i = 1; i <= totalPages; i++) {
            $pagination.append(`
                <li class="page-item ${i === currentPage ? 'active' : ''}">
                    <a class="page-link" href="#" data-page="${i}">${i}</a>
                </li>
            `);
        }
        
        // 下一页按钮
        $pagination.append(`
            <li class="page-item ${currentPage === totalPages ? 'disabled' : ''}">
                <a class="page-link" href="#" data-page="${currentPage + 1}">下一页</a>
            </li>
        `);
        
        // 绑定分页点击事件
        $('.page-link').click(function(e) {
            e.preventDefault();
            const page = $(this).data('page');
            if (page >= 1 && page <= totalPages) {
                currentPage = page;
                loadNews(currentPage);
            }
        });
    }
    
    // 编辑新闻 - 改进版
    function editNews(id) {
        
        
        // 显示加载状态
        const $modal = $('#newsModal');
        $modal.find('#modalTitle').text('加载中...');
        $modal.modal('show');
        
        // 获取新闻详情
        fetch(`/news/${id}`)
            .then(response => {
                if (!response.ok) {
                    throw new Error(`HTTP error! status: ${response.status}`);
                }
                return response.json();
            })
            .then(news => {
                
                
                if (!news || !news.n_id) {
                    throw new Error('未找到匹配的新闻');
                }
                
                // 填充表单数据
                $modal.find('#n_id').val(news.n_id);
                $modal.find('#n_title').val(news.n_title || '');
                $modal.find('#n_content').val(news.n_content || '');
                
                // 处理日期 - 使用统一格式
                try {
                    const newsDate = news.n_date ? new Date(news.n_date) : new Date();
                    if (!isNaN(newsDate.getTime())) {
                        $modal.find('#n_date').val(newsDate.toISOString().substring(0, 10));
                    } else {
                        throw new Error('Invalid date');
                    }
                } catch (e) {
                    console.error('日期解析错误:', e);
                    $modal.find('#n_date').val(new Date().toISOString().substring(0, 10));
                }
                
                                    // 处理图片预览 - 默认保留当前图片
                                    const $imagePreview = $modal.find('#imagePreview');
                                    if (news.n_pic) {
                                        // 提取纯文件名（去掉/uploads/news/前缀）
                                        const fileName = news.n_pic.replace(/^\/?uploads\/news\//, '');
                                        const imgSrc = `/uploads/news/${fileName}`;
                                        $imagePreview.html(`
                                            <div class="mb-2">
                                                <img src="${imgSrc}" class="img-thumbnail" alt="预览" style="max-height: 200px;">
                                            </div>
                                            <input type="hidden" id="currentPhotoName" value="${fileName}">
                                        `);
                                    } else {
                                        $imagePreview.html('<span class="text-muted">无图片</span>');
                                        $modal.find('#currentPhotoPath').remove();
                                    }
                
                // 清除文件选择器
                $modal.find('#n_pic').val('');
                
                // 更新模态框标题
                $modal.find('#modalTitle').text('编辑新闻');
                
                // 确保模态框完全显示
                setTimeout(() => {
                    $modal.modal('handleUpdate');
                }, 100);
            })
            .catch(error => {
                console.error('加载新闻详情失败:', error);
                $modal.modal('hide');
                showToast(`加载新闻详情失败: ${error.message}`, 'danger');
            });
    }
    
    // 格式化日期为input[type=datetime-local]需要的格式
    function formatDateForInput(dateStr) {
        const date = new Date(dateStr);
        return date.toISOString().slice(0, 16);
    }
    
    // 删除新闻
    function deleteNews(id) {
        $('#deleteId').val(id);
        $('#deleteModal').modal('show');
    }

    // 确认删除按钮事件
    $('#confirmDelete').click(function() {
        const id = $('#deleteId').val();
        
        $.ajax({
            url: `/news/delete/${id}`,
            type: 'POST',
            success: function(response) {
                $('#deleteModal').modal('hide');
                if (response.success) {
                    showToast('删除成功', 'success');
                    loadNews(currentPage);
                } else {
                    showToast(response.message || '删除失败', 'danger');
                }
            },
            error: function(xhr) {
                $('#deleteModal').modal('hide');
                let errorMsg = '删除请求失败';
                try {
                    const response = JSON.parse(xhr.responseText);
                    errorMsg = response.message || errorMsg;
                } catch (e) {}
                showToast(errorMsg, 'danger');
            }
        });
    });
    
    // 显示图片大图
    window.showImage = function(src) {
        $('#modalImage').attr('src', src);
        $('#imageModal').modal('show');
    };
    
    // 显示Toast消息
    function showToast(message, type) {
        const toastId = 'toast-' + Date.now();
        const toast = $(`
            <div id="${toastId}" class="toast align-items-center text-white bg-${type} border-0" role="alert" aria-live="assertive" aria-atomic="true">
                <div class="d-flex">
                    <div class="toast-body">
                        ${message}
                    </div>
                    <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast" aria-label="Close"></button>
                </div>
            </div>
        `);
        
        $('#toastContainer').append(toast);
        const toastObj = new bootstrap.Toast(toast[0]);
        toastObj.show();
        
        // 3秒后自动移除
        setTimeout(() => {
            toast.remove();
        }, 3000);
    }
});