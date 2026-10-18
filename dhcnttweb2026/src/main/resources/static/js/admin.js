/**
 * Admin JavaScript - macOS Sidebar & Dashboard
 */
document.addEventListener('DOMContentLoaded', function () {
  const sidebar = document.getElementById('adminSidebar');
  const toggleBtn = document.getElementById('sidebarToggleBtn');

  if (toggleBtn && sidebar) {
    toggleBtn.addEventListener('click', function () {
      sidebar.classList.toggle('collapsed');
      const isCollapsed = sidebar.classList.contains('collapsed');
      localStorage.setItem('admin_sidebar_collapsed', isCollapsed ? 'true' : 'false');
    });

    // Restore saved state
    if (localStorage.getItem('admin_sidebar_collapsed') === 'true') {
      sidebar.classList.add('collapsed');
    }
  }

  // Active button micro-interaction
  const actionButtons = document.querySelectorAll('.btn-apple-primary, .btn-apple-dark');
  actionButtons.forEach(btn => {
    btn.classList.add('apple-btn-active');
  });
});
