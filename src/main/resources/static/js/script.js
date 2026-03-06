document.addEventListener("DOMContentLoaded", () => {
  const countdownElements = document.querySelectorAll(".countdown-box");
  if (countdownElements.length >= 3) {
    let timeInSeconds = 2 * 60 * 60;

    const updateCountdown = () => {
      const hours = Math.floor(timeInSeconds / 3600);
      const minutes = Math.floor((timeInSeconds % 3600) / 60);
      const seconds = timeInSeconds % 60;

      countdownElements[0].textContent = String(hours).padStart(2, "0");
      countdownElements[1].textContent = String(minutes).padStart(2, "0");
      countdownElements[2].textContent = String(seconds).padStart(2, "0");

      if (timeInSeconds > 0) {
        timeInSeconds--;
      } else {
        timeInSeconds = 2 * 60 * 60;
      }
    };

    updateCountdown();
    setInterval(updateCountdown, 1000);
  }

  const accessoriesItem = document.querySelector(".main-menu__item--accessories");
  const megaMenu = document.querySelector(".main-menu__item--accessories .mega-menu");
  const accessoriesLink = document.querySelector(".main-menu__item--accessories .main-menu__link");

  if (accessoriesItem && megaMenu && accessoriesLink) {
    let closeTimer = null;
    const isMobile = () => window.innerWidth <= 768;

    const clearCloseTimer = () => {
      if (closeTimer) {
        clearTimeout(closeTimer);
        closeTimer = null;
      }
    };

    const setExpanded = (isOpen) => {
      accessoriesLink.setAttribute("aria-expanded", isOpen ? "true" : "false");
      accessoriesItem.classList.toggle("open", isOpen);
      megaMenu.classList.toggle("open", isOpen);
    };

    const openMenu = () => {
      clearCloseTimer();
      setExpanded(true);
    };

    const scheduleClose = () => {
      clearCloseTimer();
      closeTimer = setTimeout(() => setExpanded(false), 180);
    };

    accessoriesItem.addEventListener("mouseenter", () => {
      if (!isMobile()) openMenu();
    });

    accessoriesItem.addEventListener("mouseleave", () => {
      if (!isMobile()) scheduleClose();
    });

    megaMenu.addEventListener("mouseenter", () => {
      if (!isMobile()) openMenu();
    });

    megaMenu.addEventListener("mouseleave", () => {
      if (!isMobile()) scheduleClose();
    });

    accessoriesLink.addEventListener("click", (event) => {
      event.preventDefault();
      if (isMobile()) {
        clearCloseTimer();
        setExpanded(!accessoriesItem.classList.contains("open"));
        return;
      }
      openMenu();
    });

    document.addEventListener("click", (event) => {
      if (!accessoriesItem.contains(event.target)) {
        clearCloseTimer();
        setExpanded(false);
      }
    });

    accessoriesItem.addEventListener("focusin", () => {
      if (!isMobile()) openMenu();
    });

    accessoriesItem.addEventListener("focusout", () => {
      if (!isMobile()) scheduleClose();
    });

    accessoriesItem.addEventListener("keydown", (event) => {
      if (event.key === "Escape") {
        clearCloseTimer();
        setExpanded(false);
        accessoriesLink.focus();
      }
    });

    window.addEventListener("resize", () => {
      clearCloseTimer();
      setExpanded(false);
    });
  }
});
