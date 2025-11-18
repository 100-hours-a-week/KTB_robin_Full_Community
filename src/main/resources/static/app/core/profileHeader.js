import {logout, refresh} from "../features/auth/api.js";

(function initProfileHeader() {
    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", mountIfNeeded);
    } else {
        mountIfNeeded();
    }

    function mountIfNeeded() {
        const title = document.querySelector(".app-header .app-title");
        if (!title) return;
        if (title.querySelector(".app-profile")) return;

        const profileBtn = document.createElement("button");
        profileBtn.type = "button";
        profileBtn.className = "app-profile";
        profileBtn.dataset.state = "closed";
        profileBtn.setAttribute("aria-label", "내 프로필 메뉴 열기");
        profileBtn.setAttribute("aria-haspopup", "menu");
        profileBtn.setAttribute("aria-expanded", "false");

        const avatar = document.createElement("span");
        avatar.className = "app-profile__avatar";
        avatar.setAttribute("aria-hidden", "true");
        profileBtn.appendChild(avatar);

        const label = document.createElement("span");
        label.className = "app-profile__label";
        label.textContent = "PROFILE";
        profileBtn.appendChild(label);

        try {
            const storedUrl = localStorage.getItem("profile_image_url");
            if (storedUrl && typeof storedUrl === "string") {
                const url = storedUrl.trim();
                if (url) {
                    // Basic safety: allow absolute or root-relative URLs
                    if (url.startsWith("http://") || url.startsWith("https://") || url.startsWith("/")) {
                        avatar.style.backgroundImage = `url('${url}')`;
                        avatar.style.backgroundSize = "cover";
                        avatar.style.backgroundPosition = "center";
                        avatar.style.backgroundRepeat = "no-repeat";
                    }
                }
            }
        } catch (_) {
        }

        function handleProfileImageChanged(e) {
            try {
                const newUrl = e?.detail?.url;
                if (newUrl) {
                    avatar.style.backgroundImage = `url("${encodeURI(newUrl)}")`;
                    avatar.style.backgroundSize = "cover";
                    avatar.style.backgroundPosition = "center";
                    avatar.style.backgroundRepeat = "no-repeat";
                } else {
                    avatar.style.removeProperty("background-image");
                }
            } catch (_) {
            }
        }

        window.addEventListener("profile-image-changed", handleProfileImageChanged);

        window.addEventListener("storage", (ev) => {
            if (ev.key === "profile_image_url") {
                const newVal = ev.newValue;
                if (newVal) {
                    avatar.style.backgroundImage = `url("${encodeURI(newVal)}")`;
                    avatar.style.backgroundSize = "cover";
                    avatar.style.backgroundPosition = "center";
                    avatar.style.backgroundRepeat = "no-repeat";
                } else {
                    avatar.style.removeProperty("background-image");
                }
            }
        });

        const menu = document.createElement("nav");
        menu.id = "profileMenu";
        menu.className = "profile-menu";
        menu.setAttribute("aria-label", "프로필 메뉴");
        menu.setAttribute("role", "menu");
        menu.setAttribute("aria-hidden", "true");

        const itemInfo = document.createElement("a");
        itemInfo.className = "profile-menu__item";
        itemInfo.href = "editUserInfo.html";
        itemInfo.role = "menuitem";
        itemInfo.textContent = "회원정보수정";

        const itemPw = document.createElement("a");
        itemPw.className = "profile-menu__item";
        itemPw.href = "editPassword.html";
        itemPw.role = "menuitem";
        itemPw.textContent = "비밀번호수정";

        const itemLogout = document.createElement("button");
        itemLogout.type = "button";
        itemLogout.className = "profile-menu__item profile-menu__logout";
        itemLogout.setAttribute("role", "menuitem");
        itemLogout.textContent = "로그아웃";

        menu.appendChild(itemInfo);
        menu.appendChild(itemPw);
        menu.appendChild(itemLogout);

        title.appendChild(profileBtn);
        title.appendChild(menu);

        function openMenu() {
            menu.classList.add("is-open");
            menu.setAttribute("aria-hidden", "false");
            profileBtn.setAttribute("aria-expanded", "true");
            profileBtn.dataset.state = "open";
            document.addEventListener("click", handleOutside, {capture: true});
            document.addEventListener("keydown", handleKey);
        }

        function closeMenu() {
            menu.classList.remove("is-open");
            menu.setAttribute("aria-hidden", "true");
            profileBtn.setAttribute("aria-expanded", "false");
            profileBtn.dataset.state = "closed";
            document.removeEventListener("click", handleOutside, {capture: true});
            document.removeEventListener("keydown", handleKey);
        }

        function toggleMenu() {
            if (menu.classList.contains("is-open")) closeMenu(); else openMenu();
        }

        function handleOutside(e) {
            if (!menu.contains(e.target) && !profileBtn.contains(e.target)) {
                closeMenu();
            }
        }

        function handleKey(e) {
            if (e.key === "Escape") closeMenu();
        }

        profileBtn.addEventListener("click", toggleMenu);

        itemLogout.addEventListener("click", () => {
            try {
                logout();
            } catch (e) {
                const msg = e?.message;
                if (msg === "refresh_token_expired") {
                    refresh();
                }
            } finally {
                logout();
            }
            window.location.href = "login.html";
        });
    }
})();
