document.addEventListener("DOMContentLoaded", function() {
    const navWrap = document.getElementById("hh-nav-wrap");
    const progressBar = document.getElementById("sProgress");

    // 1. Đồng bộ hóa tiến trình cuộn trang & ghim cứng Menu độc lập
    window.addEventListener("scroll", function() {
        let scrollTop = window.scrollY;
        let docHeight = document.documentElement.scrollHeight - window.innerHeight;
        let scrollPercent = (scrollTop / docHeight) * 100;
        
        if (progressBar) progressBar.style.width = scrollPercent + "%";

        if (scrollTop > 50) {
            navWrap.classList.add("scrolled");
        } else {
            navWrap.classList.remove("scrolled");
        }
    });

    // 2. Định tuyến cấu trúc Tabs danh mục sản phẩm 
    const tabButtons = document.querySelectorAll(".tnav");
    const tabPanels = document.querySelectorAll(".tpanel");

    tabButtons.forEach(button => {
        button.addEventListener("click", function() {
            tabButtons.forEach(btn => btn.classList.remove("on"));
            tabPanels.forEach(panel => panel.classList.remove("on"));

            this.classList.add("on");
            const targetTab = this.getAttribute("data-tab");
            const activePanel = document.getElementById(targetTab);
            if (activePanel) activePanel.classList.add("on");
        });
    });

    // 3. Xử lý đóng/mở Mobile Navigation Drawer
    const hamburgerBtn = document.getElementById("hh-toggle");
    const closeBtn = document.getElementById("hh-close-btn");
    const overlay = document.getElementById("hh-overlay");
    const drawer = document.getElementById("hh-drawer");

    if (hamburgerBtn && closeBtn && overlay && drawer) {
        hamburgerBtn.addEventListener("click", function() {
            drawer.classList.add("active");
            overlay.classList.add("active");
            document.body.style.overflow = "hidden"; // Chặn cuộn trang khi menu mở
        });

        const closeDrawer = function() {
            drawer.classList.remove("active");
            overlay.classList.remove("active");
            document.body.style.overflow = ""; // Khôi phục cuộn trang
        };

        closeBtn.addEventListener("click", closeDrawer);
        overlay.addEventListener("click", closeDrawer);

        // Đóng menu khi click vào các liên kết điều hướng bên trong (trừ nút toggle accordion)
        const drawerLinks = document.querySelectorAll(".drawer-item:not(.accordion-toggle), .sub-item");
        drawerLinks.forEach(link => {
            link.addEventListener("click", closeDrawer);
        });
    }

    // 4. Xử lý đóng/mở Accordion Sub-Menu trong Mobile Drawer
    const prodToggle = document.getElementById("drawer-prod-toggle");
    const prodPanel = document.getElementById("drawer-prod-panel");
    if (prodToggle && prodPanel) {
        prodToggle.addEventListener("click", function(e) {
            e.preventDefault();
            this.classList.toggle("open");
            prodPanel.classList.toggle("open");
        });
    }

    // 5. Xử lý đóng/mở Accordion FAQ (Hỏi đáp thường gặp)
    const faqToggles = document.querySelectorAll(".faq-toggle");
    faqToggles.forEach(toggle => {
        toggle.addEventListener("click", function() {
            this.classList.toggle("open");
            const panel = this.nextElementSibling;
            if (panel) {
                if (panel.classList.contains("open")) {
                    panel.classList.remove("open");
                    panel.style.maxHeight = null;
                } else {
                    panel.classList.add("open");
                    panel.style.maxHeight = panel.scrollHeight + "px";
                }
            }
        });
    });
});

