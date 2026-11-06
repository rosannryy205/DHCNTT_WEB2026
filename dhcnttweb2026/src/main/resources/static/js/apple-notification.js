/**
 * Apple Design System - Notification Popup & Modal Engine
 * Conforms strictly to docs/DESIGN.md:
 * - 2 distinct states: Thành công (Success) and Thất bại (Error/Failure)
 * - Replaces native JavaScript alert() and confirm()
 * - Automatic Flash Message parsing from Spring Boot
 */

(function (window, document) {
  'use strict';

  // SVG Icons
  const ICONS = {
    success: `<svg viewBox="0 0 24 24"><path d="M9 16.2L4.8 12l-1.4 1.4L9 19 21 7l-1.4-1.4L9 16.2z"/></svg>`,
    error: `<svg viewBox="0 0 24 24"><path d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12 19 6.41z"/></svg>`,
    question: `<svg viewBox="0 0 24 24"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 16h-2v-2h2v2zm1.07-7.75l-.9.92C12.45 11.9 12 12.5 12 14h-2v-.5c0-1.1.45-2.1 1.17-2.83l1.24-1.26c.37-.36.59-.86.59-1.41 0-1.1-.9-2-2-2s-2 .9-2 2H7c0-2.76 2.24-5 5-5s5 2.24 5 5c0 1.04-.42 1.99-1.07 2.75z"/></svg>`,
    close: `<svg viewBox="0 0 24 24"><path d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12 19 6.41z"/></svg>`
  };

  let containerEl = null;

  function ensureContainer() {
    if (!containerEl || !document.body.contains(containerEl)) {
      containerEl = document.createElement('div');
      containerEl.className = 'apple-notification-container';
      containerEl.setAttribute('aria-live', 'assertive');
      containerEl.setAttribute('role', 'region');
      containerEl.setAttribute('aria-label', 'Thông báo hệ thống');
      document.body.appendChild(containerEl);
    }
    return containerEl;
  }

  const AppleNotification = {
    /**
     * Show a popup toast notification
     * @param {Object} options
     * @param {'success'|'error'} options.type - Trạng thái: 'success' hoặc 'error'
     * @param {string} [options.title] - Tiêu đề thông báo
     * @param {string} options.message - Nội dung chi tiết
     * @param {number} [options.duration=4000] - Thời gian hiển thị (ms), <= 0 nếu không tự đóng
     * @param {boolean} [options.dark=false] - Sử dụng theme tối HUD
     */
    show: function (options) {
      if (typeof options === 'string') {
        options = { message: options, type: 'success' };
      }

      const type = options.type === 'error' ? 'error' : 'success';
      const defaultTitle = type === 'success' ? 'Thành công' : 'Thất bại';
      const title = options.title || defaultTitle;
      const message = options.message || '';
      const duration = typeof options.duration === 'number' ? options.duration : 4000;
      const isDark = Boolean(options.dark || document.body.classList.contains('orders-admin') || document.body.classList.contains('dark-mode'));

      const container = ensureContainer();

      const toast = document.createElement('div');
      toast.className = `apple-notification-toast toast-${type}${isDark ? ' apple-dark' : ''}`;
      toast.setAttribute('role', 'alert');

      const iconWrap = document.createElement('div');
      iconWrap.className = 'apple-notification-icon';
      iconWrap.innerHTML = ICONS[type];

      const contentWrap = document.createElement('div');
      contentWrap.className = 'apple-notification-content';

      const titleEl = document.createElement('div');
      titleEl.className = 'apple-notification-title';
      titleEl.textContent = title;

      const messageEl = document.createElement('p');
      messageEl.className = 'apple-notification-message';
      messageEl.textContent = message;

      contentWrap.appendChild(titleEl);
      contentWrap.appendChild(messageEl);

      const closeBtn = document.createElement('button');
      closeBtn.className = 'apple-notification-close';
      closeBtn.type = 'button';
      closeBtn.setAttribute('aria-label', 'Đóng');
      closeBtn.innerHTML = ICONS.close;

      toast.appendChild(iconWrap);
      toast.appendChild(contentWrap);
      toast.appendChild(closeBtn);

      container.appendChild(toast);

      // Trigger animation
      requestAnimationFrame(() => {
        toast.classList.add('show');
      });

      let timer = null;

      function closeToast() {
        if (timer) clearTimeout(timer);
        toast.classList.remove('show');
        toast.classList.add('hiding');
        setTimeout(() => {
          if (toast.parentElement) {
            toast.parentElement.removeChild(toast);
          }
        }, 300);
      }

      closeBtn.addEventListener('click', closeToast);

      if (duration > 0) {
        timer = setTimeout(closeToast, duration);

        // Pause on mouse enter, resume on mouse leave
        toast.addEventListener('mouseenter', () => {
          if (timer) clearTimeout(timer);
        });

        toast.addEventListener('mouseleave', () => {
          timer = setTimeout(closeToast, Math.max(1500, duration / 2));
        });
      }

      return {
        close: closeToast,
        element: toast
      };
    },

    /**
     * Shorthand for Success notification (Trạng thái thành công)
     */
    success: function (message, title) {
      return this.show({
        type: 'success',
        title: title || 'Thành công',
        message: message
      });
    },

    /**
     * Shorthand for Error notification (Trạng thái thất bại)
     */
    error: function (message, title) {
      return this.show({
        type: 'error',
        title: title || 'Thất bại',
        message: message
      });
    },

    /**
     * Apple Confirmation Dialog replacing window.confirm()
     * @param {Object|string} options
     * @returns {Promise<boolean>}
     */
    confirm: function (options) {
      if (typeof options === 'string') {
        options = { message: options };
      }

      return new Promise((resolve) => {
        const title = options.title || 'Xác nhận';
        const message = options.message || 'Bạn có chắc chắn muốn thực hiện hành động này?';
        const confirmText = options.confirmText || 'Xác nhận';
        const cancelText = options.cancelText || 'Hủy bỏ';
        const isDanger = options.isDanger !== false; // default true for deletions

        const backdrop = document.createElement('div');
        backdrop.className = 'apple-confirm-backdrop';

        backdrop.innerHTML = `
          <div class="apple-confirm-dialog" role="dialog" aria-modal="true" aria-labelledby="appleConfirmTitle">
            <div class="apple-confirm-icon-wrap ${isDanger ? 'danger' : ''}">
              ${isDanger ? ICONS.error : ICONS.question}
            </div>
            <h3 class="apple-confirm-title" id="appleConfirmTitle">${escapeHtml(title)}</h3>
            <p class="apple-confirm-message">${escapeHtml(message)}</p>
            <div class="apple-confirm-actions">
              <button type="button" class="apple-confirm-btn apple-confirm-btn-cancel" id="appleConfirmCancel">${escapeHtml(cancelText)}</button>
              <button type="button" class="apple-confirm-btn ${isDanger ? 'apple-confirm-btn-danger' : 'apple-confirm-btn-primary'}" id="appleConfirmOk">${escapeHtml(confirmText)}</button>
            </div>
          </div>
        `;

        document.body.appendChild(backdrop);

        requestAnimationFrame(() => {
          backdrop.classList.add('show');
          const okBtn = backdrop.querySelector('#appleConfirmOk');
          if (okBtn) okBtn.focus();
        });

        function cleanup(result) {
          backdrop.classList.remove('show');
          setTimeout(() => {
            if (backdrop.parentElement) {
              backdrop.parentElement.removeChild(backdrop);
            }
            resolve(result);
          }, 250);
        }

        backdrop.querySelector('#appleConfirmOk').addEventListener('click', () => cleanup(true));
        backdrop.querySelector('#appleConfirmCancel').addEventListener('click', () => cleanup(false));

        backdrop.addEventListener('click', (e) => {
          if (e.target === backdrop) {
            cleanup(false);
          }
        });

        document.addEventListener('keydown', function escHandler(e) {
          if (e.key === 'Escape' && backdrop.parentElement) {
            document.removeEventListener('keydown', escHandler);
            cleanup(false);
          }
        });
      });
    }
  };

  function escapeHtml(str) {
    if (!str) return '';
    return String(str)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#39;');
  }

  // Replace default JS alert
  window.alert = function (msg) {
    AppleNotification.error(msg, 'Thông báo');
  };

  // Expose to window
  window.AppleNotification = AppleNotification;
  window.ApplePopup = AppleNotification; // Alias

  // Auto-detect and display Spring Boot Flash Messages on Page Load
  document.addEventListener('DOMContentLoaded', function () {
    // 1. Check hidden flash elements
    const flashSuccess = document.getElementById('appleFlashSuccess');
    if (flashSuccess && flashSuccess.dataset.message) {
      AppleNotification.success(flashSuccess.dataset.message);
    }

    const flashError = document.getElementById('appleFlashError');
    if (flashError && flashError.dataset.message) {
      AppleNotification.error(flashError.dataset.message);
    }

    const flashWarning = document.getElementById('appleFlashWarning');
    if (flashWarning && flashWarning.dataset.message) {
      AppleNotification.error(flashWarning.dataset.message, 'Lưu ý');
    }

    const flashPasswordSuccess = document.getElementById('appleFlashPasswordSuccess');
    if (flashPasswordSuccess && flashPasswordSuccess.dataset.message) {
      AppleNotification.success(flashPasswordSuccess.dataset.message);
    }

    const flashPasswordError = document.getElementById('appleFlashPasswordError');
    if (flashPasswordError && flashPasswordError.dataset.message) {
      AppleNotification.error(flashPasswordError.dataset.message);
    }

    // 2. Intercept forms or elements with data-apple-confirm
    document.addEventListener('click', function (e) {
      const confirmTrigger = e.target.closest('[data-apple-confirm]');
      if (!confirmTrigger) return;

      const confirmMessage = confirmTrigger.getAttribute('data-apple-confirm');
      const form = confirmTrigger.closest('form');

      e.preventDefault();
      e.stopPropagation();

      AppleNotification.confirm({
        title: 'Xác nhận thao tác',
        message: confirmMessage,
        confirmText: 'Xác nhận xóa',
        isDanger: true
      }).then(confirmed => {
        if (confirmed) {
          if (form) {
            form.submit();
          } else if (confirmTrigger.tagName === 'A' && confirmTrigger.href) {
            window.location.href = confirmTrigger.href;
          }
        }
      });
    }, true);
  });

})(window, document);
