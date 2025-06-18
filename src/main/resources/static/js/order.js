document.addEventListener("DOMContentLoaded", function () {
  // 更新过期预约状态为"已就诊"
  function updateExpiredOrders() {
    const now = new Date();
    const expiredOrders = Array.from(document.querySelectorAll(".appointment-item"))
      .filter(item => {
        const timeStr = item.dataset.appointmentTime;
        return timeStr && new Date(timeStr) < now;
      })
      .map(item => ({
        id: parseInt(item.dataset.appointmentId),
        status: "已就诊"
      }));

    if (expiredOrders.length) {
      fetch("/order/batchUpdateStatus", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(expiredOrders)
      }).catch(e => console.error("批量更新失败:", e));
    }
  }

  // 全局变量
  let currentPage = 1;
  const pageSize = 10;
  let totalOrders = 0;
  let isEditing = false;
  let currentStatus = ""; // 当前筛选状态

  // 初始化Bootstrap Modal (无backdrop)
  const addOrderModal = new bootstrap.Modal("#addOrderModal", {
    backdrop: false,
  });
  const editOrderModal = new bootstrap.Modal("#editOrderModal", {
    backdrop: false,
  });
  const deleteOrderModal = new bootstrap.Modal("#deleteOrderModal", {
    backdrop: false,
  });

  // DOM元素
  const addOrderBtn = document.getElementById("addOrderBtn");
  const orderForm = document.getElementById("orderForm");
  const editOrderFormContent = document.getElementById("editOrderFormContent");
  const saveOrderBtn = document.getElementById("saveOrderBtn");
  const updateOrderBtn = document.getElementById("updateOrderBtn");
  const confirmDeleteOrder = document.getElementById("confirmDeleteOrder");
  const orderTableBody = document.querySelector("#orderTable tbody");
  const pagination = document.getElementById("pagination");

  // 初始化
  loadDepartments();
  loadGenderOptions();
  loadOrders();

  // 加载性别选项
  function loadGenderOptions() {
    fetch("/order/genderOptions")
      .then(response => response.ok ? response.json() : Promise.reject('请求失败'))
      .then(options => {
        const selects = [
          document.getElementById("pGender"),
          document.getElementById("editPGender")
        ];
        
        selects.forEach(select => {
          if (select) {
            select.innerHTML = `<option value="">请选择性别</option>${
              options.map(opt => `<option value="${opt}">${opt}</option>`).join('')
            }`;
          }
        });
      })
      .catch(error => console.error('加载性别选项失败:', error));
  }

  // 事件监听
  addOrderBtn.addEventListener("click", () => {
    isEditing = false;
    orderForm.reset();
    addOrderModal.show();
  });

  saveOrderBtn.addEventListener("click", () => handleFormSubmit(false));
  updateOrderBtn.addEventListener("click", () => handleFormSubmit(true));
  confirmDeleteOrder.addEventListener("click", deleteOrder);

  // 加载科室列表
  function loadDepartments() {
    const selects = [
      document.getElementById("dName"),
      document.getElementById("editDName")
    ];

    selects.forEach(select => select.innerHTML = '<option value="">加载中...</option>');

    fetch("/department/list")
      .then(response => response.ok ? response.json() : Promise.reject('请求失败'))
      .then(departments => {
        if (!Array.isArray(departments)) throw new Error("无效的科室数据");

        const options = departments.map(dept => 
          `<option value="${dept.dName || dept.name}" data-floor="${dept.dFloor || dept.floor}">
            ${dept.dName || dept.name}
          </option>`
        ).join('');

        selects.forEach(select => {
          select.innerHTML = `<option value="">请选择科室</option>${options}`;
          select.addEventListener("change", function() {
            const floorField = this.id === 'dName' ? 
              document.getElementById("dFloor") : 
              document.getElementById("editDFloor");
            if (floorField) {
              floorField.value = this.options[this.selectedIndex]?.dataset.floor || "";
            }
            loadDoctorsByDepartment(this.value, this.id === 'editDName');
          });
        });
      })
      .catch(error => {
        selects.forEach(select => select.innerHTML = '<option value="">加载失败</option>');
        showAlert(`加载科室失败: ${error}`, "danger");
      });
  }

  // 按科室加载医生
  function loadDoctorsByDepartment(departmentName, isEditForm = false) {
    const selectId = isEditForm ? "editDocName" : "docName";
    const select = document.getElementById(selectId);
    if (!select || !departmentName) return;

    select.innerHTML = '<option value="">加载中...</option>';

    fetch(`/doctor/listByDepartment?department=${encodeURIComponent(departmentName)}`)
      .then(response => response.ok ? response.json() : Promise.reject('请求失败'))
      .then(doctors => {
        if (!Array.isArray(doctors)) throw new Error("无效数据");

        const options = doctors.map(doctor => {
          const name = doctor.docName || doctor.name || "未知医生";
          const cls = doctor.docClass || "";
          return `<option value="${name}" data-class="${cls}">${name}</option>`;
        }).join('');

        select.innerHTML = `<option value="">请选择医生</option>${options}`;
        
        // 自动填充医生职级
        select.addEventListener("change", function() {
          const classInput = document.getElementById(isEditForm ? "editDocClass" : "docClass");
          if (classInput) {
            classInput.value = this.options[this.selectedIndex]?.dataset.class || "";
          }
        });

        if (doctors.length === 0) {
          showAlert(`科室 ${departmentName} 暂无医生`, "warning");
        }
      })
      .catch(error => {
        select.innerHTML = '<option value="">加载失败</option>';
        showAlert(`加载医生失败: ${error}`, "danger");
      });
  }

  // 加载预约数据
  function loadOrders() {
    const url = `/order/page?page=${currentPage}&size=${pageSize}${
      currentStatus ? `&status=${currentStatus}` : ''
    }`;

    fetch(url)
      .then(response => response.ok ? response.json() : Promise.reject('请求失败'))
      .then(data => {
        totalOrders = data.total || 0;
        renderOrders(data.orders || []);
        updatePagination();
      })
      .catch(error => showAlert(`加载失败: ${error}`, "danger"));
  }

  // 显示提示信息
  function showAlert(message, type) {
    const alertDiv = document.createElement("div");
    alertDiv.className = `alert alert-${type} alert-dismissible fade show`;
    alertDiv.role = "alert";
    alertDiv.innerHTML = `
        ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
      `;

    const container = document.querySelector(".container") || document.body;
    container.prepend(alertDiv);

    setTimeout(() => {
      alertDiv.classList.remove("show");
      setTimeout(() => alertDiv.remove(), 150);
    }, 5000);
  }

  // 渲染预约列表
  function renderOrders(orders) {
    const orderTableBody = document.querySelector("#orderTable tbody");
    if (!orderTableBody) return;

    orderTableBody.innerHTML = orders.length ? "" : `
      <tr><td colspan="10" class="text-center">暂无预约数据</td></tr>
    `;

    orders.forEach(order => {
      const row = document.createElement("tr");
      row.className = "appointment-item";
      row.dataset.appointmentId = order.pId;
      row.dataset.appointmentTime = order.pDatetime;

      row.innerHTML = `
        <td>${order.pId}</td>
        <td>${order.pName}</td>
        <td>${order.pAge}</td>
        <td>${order.pGender || '未设置'}</td>
        <td>${order.dName}</td>
        <td>${order.docName}</td>
        <td>${order.dFloor}</td>
        <td>${order.docClass}</td>
        <td>${formatDateTime(order.pDatetime)}</td>
        <td>${order.pStatus}</td>
        <td>
          <button class="btn btn-sm btn-outline-primary btn-edit" data-id="${order.pId}">
            <i class="fas fa-edit"></i> 编辑
          </button>
          <button class="btn btn-sm btn-outline-danger btn-delete" data-id="${order.pId}">
            <i class="fas fa-trash"></i> 删除
          </button>
        </td>
      `;
      orderTableBody.appendChild(row);
    });

    // 绑定按钮事件
    document.querySelectorAll(".btn-edit").forEach(btn => {
      btn.addEventListener("click", () => showEditOrderForm(btn.dataset.id));
    });
    document.querySelectorAll(".btn-delete").forEach(btn => {
      btn.addEventListener("click", () => {
        document.getElementById("deleteOrderId").value = btn.dataset.id;
        deleteOrderModal.show();
      });
    });

    if (orders.length) updateExpiredOrders();
  }

  // 格式化日期时间 (YYYY-MM-DD HH:MM)
  function formatDateTime(dateTimeStr) {
    if (!dateTimeStr) return "";
    const date = new Date(dateTimeStr);
    const pad = num => num.toString().padStart(2, '0');
    return `${date.getFullYear()}-${pad(date.getMonth()+1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`;
  }

  // 显示编辑预约表单
  function showEditOrderForm(orderId) {
    fetch(`/order/${orderId}`)
      .then(response => response.ok ? response.json() : Promise.reject('请求失败'))
      .then(data => {
        isEditing = true;
        const order = data.order;

        // 设置基本表单值
        document.getElementById("editPId").value = order.pId;
        document.getElementById("editPName").value = order.pName;
        document.getElementById("editPAge").value = order.pAge;
        document.getElementById("editPGender").value = order.pGender || "";
        document.getElementById("editPStatus").value = order.pStatus;

        // 设置科室和楼层
        const deptSelect = document.getElementById("editDName");
        deptSelect.value = order.dName;
        document.getElementById("editDFloor").value = order.dFloor;

        // 触发科室变更事件加载医生
        deptSelect.dispatchEvent(new Event("change"));

        // 设置医生和职称
        const doctorSelect = document.getElementById("editDocName");
        const checkAndSetDoctor = () => {
          if (doctorSelect.options.length > 0) {
            doctorSelect.value = order.docName;
            document.getElementById("editDocClass").value = order.docClass;
          } else {
            setTimeout(checkAndSetDoctor, 100);
          }
        };
        checkAndSetDoctor();

        // 设置预约时间
        if (order.pDatetime) {
          const date = new Date(order.pDatetime);
          if (!isNaN(date.getTime())) {
            document.getElementById("editPDatetime").value = 
              `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`;
          }
        }

        editOrderModal.show();
      })
      .catch(error => showAlert(`获取预约详情失败: ${error}`, "danger"));
  }

  // 处理时间输入，固定分钟为00
  function handleTimeInput(inputId) {
    const input = document.getElementById(inputId);
    if (input && input.value) {
      const date = new Date(input.value);
      if (!isNaN(date.getTime())) {
        date.setMinutes(0);
        input.value = date.toISOString().slice(0, 16);
      }
    }
  }

  // 通用表单处理函数
  function handleFormSubmit(isEdit = false) {
    const form = isEdit ? editOrderFormContent : orderForm;
    const btn = isEdit ? updateOrderBtn : saveOrderBtn;
    const modal = isEdit ? editOrderModal : addOrderModal;
    const url = isEdit ? "/order/update" : "/order/add";
    const prefix = isEdit ? "edit" : "";

    // 处理时间输入
    handleTimeInput(`${prefix}pDatetime`);

    // 表单验证
    form.classList.add("was-validated");
    if (!form.checkValidity()) return;

    // 安全获取表单数据
    const getFormValue = (id) => {
      // 根据是否编辑模式决定前缀和大小写
      const fullId = isEdit ? `edit${id.charAt(0).toUpperCase() + id.slice(1)}` : id;
      const el = document.getElementById(fullId);
      if (!el) {
        console.error(`表单元素 ${fullId} 未找到`);
        return null;
      }
      return el.value;
    };

    const formData = {
      ...(isEdit && { pId: getFormValue("pId") }),
      pName: getFormValue("pName")?.trim(),
      pAge: getFormValue("pAge"),
      pGender: getFormValue("pGender"),
      dName: getFormValue("dName"),
      docName: getFormValue("docName"),
      dFloor: getFormValue("dFloor"),
      docClass: getFormValue("docClass"),
      pDatetime: getFormValue("pDatetime"),
      pStatus: getFormValue("pStatus"),
    };

    // 验证必填字段
    const requiredFields = ["pName", "dName", "docName", "pDatetime"];
    const missingFields = requiredFields.filter(field => !formData[field]);
    if (missingFields.length) {
      showAlert(`请填写以下必填字段: ${missingFields.join(", ")}`, "danger");
      return;
    }

    // 验证必填字段
    if (!formData.pName || !formData.dName || !formData.docName || !formData.pDatetime) {
      showAlert("请填写所有必填字段", "danger");
      return;
    }

    // 显示加载状态
    btn.disabled = true;
    btn.innerHTML = '<span class="spinner-border spinner-border-sm"></span> 处理中...';

    fetch(url, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(formData)
    })
      .then(response => response.ok ? response.json() : Promise.reject('请求失败'))
      .then(data => {
        if (data.success) {
          showAlert(`预约${isEdit ? '更新' : '添加'}成功`, "success");
          modal.hide();
          loadOrders();
        } else {
          showAlert(data.message || `预约${isEdit ? '更新' : '添加'}失败`, "danger");
        }
      })
      .catch(error => showAlert(error, "danger"))
      .finally(() => {
        btn.disabled = false;
        btn.innerHTML = isEdit ? "更新" : "保存";
      });
  }

  // 确保只绑定一次事件
  if (!saveOrderBtn.dataset.listenerAdded) {
    saveOrderBtn.addEventListener("click", () => {
      if (saveOrderBtn.disabled) return;
      handleFormSubmit(false);
    });
    saveOrderBtn.dataset.listenerAdded = "true";
  }

  if (!updateOrderBtn.dataset.listenerAdded) {
    updateOrderBtn.addEventListener("click", () => {
      if (updateOrderBtn.disabled) return;
      handleFormSubmit(true);
    });
    updateOrderBtn.dataset.listenerAdded = "true";
  }

  // 删除预约
  function deleteOrder() {
    const orderId = document.getElementById("deleteOrderId").value;

    fetch(`/order/delete/${orderId}`, { method: "DELETE" })
      .then(response => response.ok ? response.json() : Promise.reject('删除失败'))
      .then(data => {
        showAlert(data.message || "预约删除成功", "success");
        deleteOrderModal.hide();
        loadOrders();
      })
      .catch(error => showAlert(`删除失败: ${error}`, "danger"));
  }

  // 更新分页信息
  function updatePagination() {
    const totalPages = Math.ceil(totalOrders / pageSize);
    pagination.innerHTML = "";

    // 上一页按钮
    const prevLi = document.createElement("li");
    prevLi.className = `page-item ${currentPage === 1 ? "disabled" : ""}`;
    prevLi.innerHTML = `<a class="page-link" href="#">上一页</a>`;
    prevLi.addEventListener("click", (e) => {
      e.preventDefault();
      if (currentPage > 1) {
        currentPage--;
        loadOrders();
      }
    });
    pagination.appendChild(prevLi);

    // 页码按钮
    for (let i = 1; i <= totalPages; i++) {
      const pageLi = document.createElement("li");
      pageLi.className = `page-item ${i === currentPage ? "active" : ""}`;
      pageLi.innerHTML = `<a class="page-link" href="#">${i}</a>`;
      pageLi.addEventListener("click", (e) => {
        e.preventDefault();
        currentPage = i;
        loadOrders();
      });
      pagination.appendChild(pageLi);
    }

    // 下一页按钮
    const nextLi = document.createElement("li");
    nextLi.className = `page-item ${
      currentPage === totalPages || totalOrders === 0 ? "disabled" : ""
    }`;
    nextLi.innerHTML = `<a class="page-link" href="#">下一页</a>`;
    nextLi.addEventListener("click", (e) => {
      e.preventDefault();
      if (currentPage < totalPages) {
        currentPage++;
        loadOrders();
      }
    });
    pagination.appendChild(nextLi);
  }
});