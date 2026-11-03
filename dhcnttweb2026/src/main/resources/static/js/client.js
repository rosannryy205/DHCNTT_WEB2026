/**
 * Client JavaScript - Apple E-commerce interactions
 */
document.addEventListener('DOMContentLoaded', function () {
  'use strict';

  // 1. Add scale micro-interaction for Apple buttons
  const appleButtons = document.querySelectorAll('.btn-apple-primary, .btn-apple-secondary, .btn-apple-dark');
  appleButtons.forEach(btn => {
    btn.classList.add('apple-btn-active');
  });

  // 2. Sticky sub-nav shadow/border enhancement on scroll
  const subNav = document.querySelector('.apple-sub-nav');
  if (subNav) {
    window.addEventListener('scroll', () => {
      if (window.scrollY > 44) {
        subNav.style.boxShadow = '0 2px 10px rgba(0, 0, 0, 0.05)';
      } else {
        subNav.style.boxShadow = 'none';
      }
    });
  }

  // 3. Load and update Bag Count on Global Nav
  const navBagBadge = document.getElementById('globalNavBagBadge') || document.querySelector('.apple-global-nav .bi-bag + .badge');
  if (navBagBadge) {
    fetch('/api/bag/count')
      .then(res => res.json())
      .then(data => {
        if (typeof data.count === 'number') {
          navBagBadge.textContent = data.count;
          navBagBadge.style.display = data.count > 0 ? 'inline-block' : 'none';
        }
      })
      .catch(err => console.debug('Bag count fetch skipped', err));
  }

  // 4. Apple Mega Nav Flyout Hover Intent & Interaction Controller
  // Provides a 300ms grace period so mouse movements into flyouts never cause dropouts.
  const navItems = document.querySelectorAll('.apple-nav-item');
  const navBackdrop = document.querySelector('.apple-nav-backdrop');
  let flyoutCloseTimer = null;

  navItems.forEach(item => {
    const flyout = item.querySelector('.apple-nav-flyout');
    if (!flyout) return;

    // Khi di chuột vào danh mục cha: Mở flyout ngay lập tức
    item.addEventListener('mouseenter', () => {
      clearTimeout(flyoutCloseTimer);
      navItems.forEach(other => {
        if (other !== item) other.classList.remove('is-flyout-open');
      });
      item.classList.add('is-flyout-open');
    });

    // Khi di chuột rời khỏi danh mục cha: Chờ 300ms để cho phép chuột di chuyển vào flyout
    item.addEventListener('mouseleave', () => {
      clearTimeout(flyoutCloseTimer);
      flyoutCloseTimer = setTimeout(() => {
        item.classList.remove('is-flyout-open');
      }, 300);
    });

    // Khi chuột đã vào trong flyout menu: Giữ nguyên trạng thái mở
    flyout.addEventListener('mouseenter', () => {
      clearTimeout(flyoutCloseTimer);
      item.classList.add('is-flyout-open');
    });

    // Khi chuột rời khỏi flyout menu: Đóng sau 300ms
    flyout.addEventListener('mouseleave', () => {
      clearTimeout(flyoutCloseTimer);
      flyoutCloseTimer = setTimeout(() => {
        item.classList.remove('is-flyout-open');
      }, 300);
    });
  });

  // Đóng flyout ngay lập tức khi click vào vùng backdrop mờ
  if (navBackdrop) {
    navBackdrop.addEventListener('click', () => {
      clearTimeout(flyoutCloseTimer);
      navItems.forEach(item => item.classList.remove('is-flyout-open'));
    });
  }
});
