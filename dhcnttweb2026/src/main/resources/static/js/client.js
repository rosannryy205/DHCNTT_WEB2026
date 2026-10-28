/**
 * Client JavaScript - Apple E-commerce interactions
 */
document.addEventListener('DOMContentLoaded', function () {
  // Add scale micro-interaction for Apple buttons
  const appleButtons = document.querySelectorAll('.btn-apple-primary, .btn-apple-secondary, .btn-apple-dark');
  appleButtons.forEach(btn => {
    btn.classList.add('apple-btn-active');
  });

  // Sticky sub-nav shadow/border enhancement on scroll
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

  // Load and update Bag Count on Global Nav
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
});
