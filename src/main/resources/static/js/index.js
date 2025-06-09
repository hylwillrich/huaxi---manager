/**
 * 处理用户信息显示 - 将导航栏用户信息移动到内容区顶部
 */
document.addEventListener('DOMContentLoaded', function() {
    // 获取导航栏中的用户信息元素
    const navUserInfo = document.querySelector('.navbar .d-flex');
    if (!navUserInfo) return;

    // 获取main-content元素
    const mainContent = document.getElementById('main-content');
    if (!mainContent) return;

    // 克隆用户信息元素
    const userInfoClone = navUserInfo.cloneNode(true);
    
    // 提取纯文本内容作为data-user属性值
    let userText = '';
    userInfoClone.childNodes.forEach(node => {
        if (node.nodeType === Node.TEXT_NODE) {
            userText += node.textContent.trim() + ' ';
        } else if (node.nodeType === Node.ELEMENT_NODE && node.tagName !== 'BUTTON') {
            userText += node.textContent.trim() + ' ';
        }
    });
    
    // 设置data-user属性
    mainContent.setAttribute('data-user', userText.trim());

    // 确保按钮保留在克隆中
    const buttons = userInfoClone.querySelectorAll('button');
    buttons.forEach(button => {
        button.classList.add('btn');
    });
});

// 可以在此添加其他页面相关JavaScript代码