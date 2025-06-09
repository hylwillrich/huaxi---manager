// 显示指定表单
function showForm(formId) {
    document.getElementById(formId).style.display = 'block';
}

// 隐藏指定表单
function hideForm(formId) {
    document.getElementById(formId).style.display = 'none';
}

// 显示Toast消息
function showToast(message, type = 'success') {
    const toast = document.createElement('div');
    toast.className = `toast align-items-center text-white bg-${type} border-0`;
    toast.setAttribute('role', 'alert');
    toast.setAttribute('aria-live', 'assertive');
    toast.setAttribute('aria-atomic', 'true');
    
    toast.innerHTML = `
        <div class="d-flex">
            <div class="toast-body">${message}</div>
            <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast" aria-label="Close"></button>
        </div>
    `;
    
    const toastContainer = document.getElementById('toastContainer') || (() => {
        const container = document.createElement('div');
        container.id = 'toastContainer';
        container.style.position = 'fixed';
        container.style.top = '20px';
        container.style.right = '20px';
        container.style.zIndex = '9999';
        document.body.appendChild(container);
        return container;
    })();
    
    toastContainer.appendChild(toast);
    const bsToast = new bootstrap.Toast(toast);
    bsToast.show();
    
    toast.addEventListener('hidden.bs.toast', () => {
        toast.remove();
    });
}

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
        
        const formData = new FormData(form);
        const data = Object.fromEntries(formData.entries());
        
        // 转换布尔值
        if (data.isOpen) {
            data.isOpen = data.isOpen === 'true';
        }
        
        const response = await fetch(url, {
            method: method,
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(data)
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

// 初始化页面事件
document.addEventListener('DOMContentLoaded', function() {
    // 添加科室按钮点击事件
    document.getElementById('addBtn').addEventListener('click', function() {
        hideForm('editForm');
        showForm('addForm');
    });

    // 添加表单提交
    document.getElementById('addDepartmentForm').addEventListener('submit', (e) => {
        handleFormSubmit(e, '/department/add', 'POST');
    });

    // 编辑表单提交
    document.getElementById('editDepartmentForm').addEventListener('submit', (e) => {
        handleFormSubmit(e, '/department/update', 'POST');
    });

    // 编辑按钮点击事件
    document.querySelectorAll('.btn-warning').forEach(btn => {
        btn.addEventListener('click', function() {
            const row = this.closest('tr');
            // 填充编辑表单数据
            document.getElementById('editId').value = row.cells[0].textContent;
            document.getElementById('editName').value = row.cells[1].textContent;
            document.getElementById('editFloor').value = row.cells[2].textContent;
            document.getElementById('editTelephone').value = row.cells[3].textContent;
            document.getElementById('editIsOpen').value = row.cells[4].querySelector('.badge').textContent === '开放' ? 'true' : 'false';
            
            hideForm('addForm');
            showForm('editForm');
        });
    });

    // 删除按钮点击事件
    document.querySelectorAll('.btn-danger').forEach(btn => {
        btn.addEventListener('click', function() {
            const row = this.closest('tr');
            document.getElementById('deleteId').value = row.cells[0].textContent;
            
            // 使用Bootstrap模态框
            const deleteModal = new bootstrap.Modal(document.getElementById('deleteModal'));
            deleteModal.show();
        });
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
            
            const response = await fetch(`/department/delete/${id}`, {
                method: 'POST'
            });
            
            if (!response.ok) {
                throw new Error(await response.text());
            }
            
            const result = await response.json();
            if (result) {
                showToast('删除成功!');
                setTimeout(() => location.reload(), 1000);
            } else {
                showToast('删除失败，请重试', 'danger');
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