// 切换表单显示
const toggleForm = (formId, show = true) => {
    document.querySelectorAll('.form-container').forEach(f => f.style.display = 'none');
    if (show) document.getElementById(formId).style.display = 'block';
};

// 加载科室选项
async function loadDepartmentOptions() {
    try {
        const response = await fetch('/doctor/department/list');
        const departments = await response.json();
        
        const addSelect = document.getElementById('addDName');
        const editSelect = document.getElementById('editDName');
        
        
        // 清空现有选项
        addSelect.innerHTML = '';
        editSelect.innerHTML = '';
        
        // 添加默认选项
        const defaultOption = document.createElement('option');
        defaultOption.value = '';
        defaultOption.textContent = '请选择科室';
        defaultOption.selected = true;
        defaultOption.disabled = true;
        addSelect.appendChild(defaultOption.cloneNode(true));
        editSelect.appendChild(defaultOption.cloneNode(true));
        
        // 添加科室选项
        departments.forEach(dept => {
            const option = document.createElement('option');
            option.value = dept.name;  // 使用科室名称作为value
            option.textContent = dept.name;
            addSelect.appendChild(option.cloneNode(true));
            editSelect.appendChild(option);
        });
    } catch (error) {
        console.error('加载科室选项失败:', error);
        showToast('加载科室信息失败，请刷新页面重试', 'danger');
    }
}

// 加载医生数据
let currentPage = 1;
const pageSize = 10;

async function loadDoctors(page = 1) {
    try {
        currentPage = page;
        const response = await fetch(`/doctor/page?page=${page}&size=${pageSize}`);
        const data = await response.json();
        const doctors = data.doctors;
        const tableBody = document.getElementById('doctorTableBody');
        
        // 清空表格
        tableBody.innerHTML = '';
        
        // 渲染分页导航
        renderPagination(currentPage, Math.ceil(data.total / pageSize));
        
        // 填充表格
        doctors.forEach(doctor => {
            const row = document.createElement('tr');
            
            // 照片显示
            const photoCell = document.createElement('td');
            if (doctor.docPhoto) {
                const img = document.createElement('img');
                img.src = doctor.docPhoto;
                img.alt = doctor.docName;
                img.className = 'doctor-photo';
                photoCell.appendChild(img);
            } else {
                photoCell.textContent = '无照片';
            }
            
            // 此处did和dname都是小写,  其他为驼峰写法
            row.innerHTML = `
                <td>${doctor.docId}</td>
                <td>${doctor.docName}</td>
                <td>${doctor.dname}</td>
                <td>${doctor.docGender}</td>
                <td>${doctor.docAge}</td>
            `;
            row.appendChild(photoCell);
            row.innerHTML += `
                <td>${doctor.docClass}</td>
                <td class="action-buttons">
                    <button class="btn btn-warning btn-sm me-1 edit-btn" data-id="${doctor.docId}">
                        <i class="fa fa-edit"></i> 编辑
                    </button>
                    <button class="btn btn-danger btn-sm delete-btn" data-id="${doctor.docId}">
                        <i class="fa fa-trash"></i> 删除
                    </button>
                </td>
            `;
            
            tableBody.appendChild(row);
        });
        
        // 添加编辑按钮事件
        document.querySelectorAll('.edit-btn').forEach(btn => {
            btn.addEventListener('click', function() {
                const doctor = doctors.find(d => d.docId == this.dataset.id);
                if (doctor) {
                    // 填充编辑表单
                    document.getElementById('editId').value = doctor.docId;
                    document.getElementById('editName').value = doctor.docName;
                    // 设置科室预选值
                    const deptSelect = document.getElementById('editDName');
                    
                    // 检查元素是否存在
                    if (!deptSelect) {
                        console.error('科室选择框元素未找到');
                        return;
                    }
                    
                    // 确保科室选项已加载
                    const setDeptValue = () => {
                        if (!deptSelect) return;
                        
                        if (deptSelect.querySelector(`option[value="${doctor.dname}"]`)) {
                            deptSelect.value = doctor.dname;
                        } else {
                            // 如果科室选项不存在，先加载科室选项
                            loadDepartmentOptions().then(() => {
                                if (!deptSelect) return;
                                
                                // 再次检查确保选项已加载
                                if (deptSelect.querySelector(`option[value="${doctor.dname}"]`)) {
                                    deptSelect.value = doctor.dname;
                                } else {
                                    console.warn(`科室名称 ${doctor.dname} 不存在于选项中`);
                                }
                            });
                        }
                    };
                    
                    // 如果科室选项已加载，直接设置值
                    if (deptSelect.options && deptSelect.options.length > 1) {
                        setDeptValue();
                    } else {
                        // 如果科室选项未加载，先加载再设置
                        loadDepartmentOptions().then(setDeptValue);
                    }
                    
                    document.getElementById('editGender').value = doctor.docGender;
                    document.getElementById('editAge').value = doctor.docAge;
                    document.getElementById('editClass').value = doctor.docClass;
                    
                    // 显示当前照片
                    const currentPhoto = document.getElementById('currentPhoto');
                    currentPhoto.innerHTML = '';
                    if (doctor.docPhoto) {
                        const img = document.createElement('img');
                        img.src = doctor.docPhoto;
                        img.alt = '当前照片';
                        img.className = 'doctor-photo';
                        img.style.width = '100px';
                        img.style.height = '100px';
                        currentPhoto.appendChild(img);
                    } else {
                        currentPhoto.textContent = '当前无照片';
                    }
                    
                    toggleForm('editForm');
                }
            });
        });
        
        // 添加删除按钮事件
        document.querySelectorAll('.delete-btn').forEach(btn => {
            btn.addEventListener('click', function() {
                document.getElementById('deleteId').value = this.dataset.id;
                const deleteModal = new bootstrap.Modal(document.getElementById('deleteModal'));
                deleteModal.show();
            });
        });
    } catch (error) {
        console.error('加载医生数据失败:', error);
        showToast('加载医生信息失败', 'danger');
    }
}

// 显示Toast消息
const showToast = (message, type = 'success') => {
    const container = document.getElementById('toastContainer') || (() => {
        const el = document.createElement('div');
        el.id = 'toastContainer';
        Object.assign(el.style, {
            position: 'fixed',
            top: '20px',
            right: '20px',
            zIndex: '9999'
        });
        document.body.appendChild(el);
        return el;
    })();

    const toast = document.createElement('div');
    toast.className = `toast show align-items-center text-white bg-${type} border-0`;
    toast.innerHTML = `
        <div class="d-flex">
            <div class="toast-body">${message}</div>
            <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button>
        </div>
    `;
    container.appendChild(toast);
    setTimeout(() => toast.remove(), 3000);
};

// 处理表单提交
async function handleFormSubmit(event, url, method) {
    event.preventDefault();
    
    const form = event.target;
    const submitBtn = form.querySelector('button[type="submit"]');
    const originalText = submitBtn.innerHTML;
    
    try {
        // 显示加载状态
        submitBtn.disabled = true;
        submitBtn.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> 处理中...';
        
        // 创建FormData对象并调试
        const formData = new FormData(form);
        console.log('FormData内容:');
        for (let [key, value] of formData.entries()) {
            console.log(key, value);
        }
        
        // 添加科室名称参数
        const deptSelect = form.querySelector('select[name="dName"]');
        if (!deptSelect) {
            console.error('科室选择框元素未找到');
            showToast('表单数据异常，请刷新页面重试', 'danger');
            return;
        }
        const dName = deptSelect.value;
        formData.append('dName', dName);
        
        // 如果是编辑表单且没有选择新照片，保留原照片
        if (method === 'POST' && url.includes('update')) {
            const fileInput = form.querySelector('input[type="file"]');
            if (fileInput.files.length === 0) {
                const currentPhoto = document.getElementById('currentPhoto').querySelector('img');
                if (currentPhoto) {
                    const photoPath = currentPhoto.src.split('/').pop();
                    formData.append('currentPhoto', photoPath);
                }
            }
        }
        
        const response = await fetch(url, {
            method: method,
            body: formData
        });
        
        if (!response.ok) {
            throw new Error(await response.text());
        }
        
        const result = await response.json();
        if (result.success) {
            showToast('操作成功!');
            setTimeout(() => location.reload(), 1000);
        } else {
            showToast('操作失败，请重试', 'danger');
        }
    } catch (error) {
        console.error('Error:', error);
        showToast(`操作失败: ${error.message}`, 'danger');
    } finally {
        submitBtn.disabled = false;
        submitBtn.innerHTML = originalText;
    }
}

// 渲染分页导航
function renderPagination(currentPage, totalPages) {
    const paginationContainer = document.getElementById('paginationContainer') || createPaginationContainer();
    paginationContainer.innerHTML = '';
    
    if (totalPages <= 1) return;
    
    // 创建分页列表
    const paginationList = document.createElement('ul');
    paginationList.className = 'pagination justify-content-center';
    
    // 上一页按钮
    const prevItem = document.createElement('li');
    prevItem.className = `page-item ${currentPage === 1 ? 'disabled' : ''}`;
    prevItem.innerHTML = `<a class="page-link" href="#" data-page="${currentPage - 1}">上一页</a>`;
    paginationList.appendChild(prevItem);
    
    // 页码按钮
    for (let i = 1; i <= totalPages; i++) {
        const pageItem = document.createElement('li');
        pageItem.className = `page-item ${i === currentPage ? 'active' : ''}`;
        pageItem.innerHTML = `<a class="page-link" href="#" data-page="${i}">${i}</a>`;
        paginationList.appendChild(pageItem);
    }
    
    // 下一页按钮
    const nextItem = document.createElement('li');
    nextItem.className = `page-item ${currentPage === totalPages ? 'disabled' : ''}`;
    nextItem.innerHTML = `<a class="page-link" href="#" data-page="${currentPage + 1}">下一页</a>`;
    paginationList.appendChild(nextItem);
    
    paginationContainer.appendChild(paginationList);
}

function createPaginationContainer() {
    const container = document.createElement('div');
    container.id = 'paginationContainer';
    container.className = 'mt-3';
    document.querySelector('.table-responsive').after(container);
    return container;
}

// 初始化页面
document.addEventListener('DOMContentLoaded', function() {
    // 加载科室选项和医生数据
    loadDepartmentOptions();
    loadDoctors();
    
    // 分页按钮事件委托
    document.addEventListener('click', function(e) {
        if (e.target.classList.contains('page-link')) {
            e.preventDefault();
            const page = parseInt(e.target.dataset.page);
            if (!isNaN(page)) {
                loadDoctors(page);
            }
        }
    });
    
    // 添加医生按钮点击事件
    document.getElementById('addBtn').addEventListener('click', function() {
        toggleForm('addForm');
    });
    
    // 添加表单提交
    document.getElementById('addDoctorForm').addEventListener('submit', (e) => {
        handleFormSubmit(e, '/doctor/add', 'POST');
    });
    
    // 编辑表单提交
    document.getElementById('editDoctorForm').addEventListener('submit', (e) => {
        handleFormSubmit(e, '/doctor/update', 'POST');
    });
    
    // 删除确认按钮
    document.getElementById('confirmDelete').addEventListener('click', async function() {
        const deleteBtn = this;
        const originalText = deleteBtn.innerHTML;
        const id = document.getElementById('deleteId').value;
        
        try {
            // 显示加载状态
            deleteBtn.disabled = true;
            deleteBtn.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> 删除中...';
            
            const response = await fetch(`/doctor/delete/${id}`, {
                method: 'POST'
            });
            
            if (!response.ok) {
                throw new Error(await response.text());
            }
            
            const result = await response.json();
            if (result.success) {
                showToast('删除成功!');
                setTimeout(() => location.reload(), 1000);
            } else {
                showToast('删除失败', 'danger');
            }
        } catch (error) {
            console.error('Error:', error);
            showToast(`删除失败: ${error.message}`, 'danger');
        } finally {
            deleteBtn.disabled = false;
            deleteBtn.innerHTML = originalText;
            bootstrap.Modal.getInstance(document.getElementById('deleteModal')).hide();
        }
    });
});