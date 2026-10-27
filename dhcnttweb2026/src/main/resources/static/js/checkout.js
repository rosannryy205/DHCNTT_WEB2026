/**
 * Apple Store Checkout Interaction Script
 * Handles:
 * - Payment method switching (cod, vnpay, momo matching database ENUM)
 * - Province / District / Ward dynamic cascade and full shippingAddress generation
 * - Order note character counter (max 255 chars)
 * - VAT electronic invoice drawer toggle
 * - Shipping method selection
 * - Promo code application (e.g. APPLEVIP)
 * - Client-side form validation before submission
 */

document.addEventListener("DOMContentLoaded", function () {
  initPaymentMethodSwitcher();
  initVietnamAddressCascade();
  initOrderNoteCounter();
  initVatToggle();
  initShippingOptions();
  initPromoCode();
  initFormValidation();
});

/**
 * 1. Payment Method Switcher (cod, vnpay, momo)
 */
function initPaymentMethodSwitcher() {
  const methodCards = document.querySelectorAll(".payment-method-card");
  const radioInputs = document.querySelectorAll('input[name="paymentMethod"]');

  methodCards.forEach((card) => {
    card.addEventListener("click", function (e) {
      const radio = card.querySelector('input[type="radio"]');
      if (radio) {
        radio.checked = true;
        updateSelectedPaymentCard(radio.value);
      }
    });
  });

  radioInputs.forEach((radio) => {
    radio.addEventListener("change", function () {
      updateSelectedPaymentCard(this.value);
    });
  });

  function updateSelectedPaymentCard(selectedMethod) {
    methodCards.forEach((c) => {
      const r = c.querySelector('input[type="radio"]');
      if (r && r.value === selectedMethod) {
        c.classList.add("selected");
      } else {
        c.classList.remove("selected");
      }
    });
  }
}

/**
 * 2. Vietnam Administrative Address Hierarchy
 * Updates province -> district -> ward and synchronizes into #shippingAddress (orders.shipping_address)
 */
function initVietnamAddressCascade() {
  const provinceSelect = document.getElementById("provinceSelect");
  const districtSelect = document.getElementById("districtSelect");
  const wardSelect = document.getElementById("wardSelect");
  const streetInput = document.getElementById("streetAddress");
  const fullAddressInput = document.getElementById("shippingAddress");

  if (!provinceSelect || !districtSelect || !wardSelect || !streetInput) return;

  const addressData = {
    "Thành phố Hà Nội": {
      "Quận Hoàn Kiếm": ["Phường Tràng Tiền", "Phường Hàng Bạc", "Phường Hàng Trống", "Phường Cửa Nam"],
      "Quận Ba Đình": ["Phường Kim Mã", "Phường Liễu Giai", "Phường Điện Biên", "Phường Cống Vị"],
      "Quận Đống Đa": ["Phường Láng Hạ", "Phường Ô Chợ Dừa", "Phường Nam Đồng", "Phường Trung Tự"],
      "Quận Cầu Giấy": ["Phường Dịch Vọng", "Phường Yên Hòa", "Phường Mai Dịch", "Phường Quan Hoa"],
      "Quận Hà Đông": ["Phường Mộ Lao", "Phường Văn Quán", "Phường Quang Trung", "Phường Vạn Phúc"]
    },
    "Thành phố Hồ Chí Minh": {
      "Quận 1": ["Phường Bến Nghé", "Phường Bến Thành", "Phường Đa Kao", "Phường Tân Định"],
      "Quận Bình Thạnh": ["Phường 22 (Landmark 81)", "Phường 25", "Phường 19", "Phường 26"],
      "Quận 5": ["Phường 1", "Phường 2", "Phường 4 (ĐH Sư Phạm)", "Phường 7"],
      "Quận 7": ["Phường Tân Phong (Phú Mỹ Hưng)", "Phường Tân Phú", "Phường Tân Hưng"],
      "Thành phố Thủ Đức": ["Phường Thảo Điền", "Phường An Phú", "Phường Linh Trung", "Phường Hiệp Phú"]
    },
    "Thành phố Đà Nẵng": {
      "Quận Hải Châu": ["Phường Thạch Thang", "Phường Hải Châu 1", "Phường Phước Ninh"],
      "Quận Sơn Trà": ["Phường An Hải Bắc", "Phường Phước Mỹ", "Phường Thọ Quang"],
      "Quận Thanh Khê": ["Phường Vĩnh Trung", "Phường Chính Gián", "Phường Tân Chính"]
    },
    "Thành phố Cần Thơ": {
      "Quận Ninh Kiều": ["Phường Tân An", "Phường An Cư", "Phường Xuân Khánh"],
      "Quận Cái Răng": ["Phường Hưng Phú", "Phường Lê Bình"]
    },
    "Thành phố Hải Phòng": {
      "Quận Hồng Bàng": ["Phường Hoàng Văn Thụ", "Phường Minh Khai"],
      "Quận Lê Chân": ["Phường An Biên", "Phường Cát Dài"]
    }
  };

  // Populate districts when province changes
  provinceSelect.addEventListener("change", function () {
    const selectedProv = this.value;
    districtSelect.innerHTML = '<option value="" disabled selected>Chọn Quận / Huyện</option>';
    wardSelect.innerHTML = '<option value="" disabled selected>Chọn Phường / Xã</option>';

    if (addressData[selectedProv]) {
      Object.keys(addressData[selectedProv]).forEach((dist) => {
        const opt = document.createElement("option");
        opt.value = dist;
        opt.textContent = dist;
        districtSelect.appendChild(opt);
      });
      districtSelect.disabled = false;
    } else {
      districtSelect.disabled = true;
      wardSelect.disabled = true;
    }
    updateFullShippingAddress();
  });

  // Populate wards when district changes
  districtSelect.addEventListener("change", function () {
    const selectedProv = provinceSelect.value;
    const selectedDist = this.value;
    wardSelect.innerHTML = '<option value="" disabled selected>Chọn Phường / Xã</option>';

    if (addressData[selectedProv] && addressData[selectedProv][selectedDist]) {
      addressData[selectedProv][selectedDist].forEach((w) => {
        const opt = document.createElement("option");
        opt.value = w;
        opt.textContent = w;
        wardSelect.appendChild(opt);
      });
      wardSelect.disabled = false;
    } else {
      wardSelect.disabled = true;
    }
    updateFullShippingAddress();
  });

  wardSelect.addEventListener("change", updateFullShippingAddress);
  streetInput.addEventListener("input", updateFullShippingAddress);

  function updateFullShippingAddress() {
    const street = streetInput.value.trim();
    const ward = wardSelect.value;
    const district = districtSelect.value;
    const province = provinceSelect.value;

    const parts = [];
    if (street) parts.push(street);
    if (ward) parts.push(ward);
    if (district) parts.push(district);
    if (province) parts.push(province);

    const fullStr = parts.join(", ");
    if (fullAddressInput) {
      fullAddressInput.value = fullStr;
    }
  }

  // Pre-trigger on load if province has default value
  if (provinceSelect.value && addressData[provinceSelect.value]) {
    const currentDist = districtSelect.getAttribute("data-current") || "Quận Hoàn Kiếm";
    const currentWard = wardSelect.getAttribute("data-current") || "Phường Tràng Tiền";

    // Populate districts
    Object.keys(addressData[provinceSelect.value]).forEach((dist) => {
      const opt = document.createElement("option");
      opt.value = dist;
      opt.textContent = dist;
      if (dist === currentDist) opt.selected = true;
      districtSelect.appendChild(opt);
    });
    districtSelect.disabled = false;

    // Populate wards
    if (addressData[provinceSelect.value][currentDist]) {
      addressData[provinceSelect.value][currentDist].forEach((w) => {
        const opt = document.createElement("option");
        opt.value = w;
        opt.textContent = w;
        if (w === currentWard) opt.selected = true;
        wardSelect.appendChild(opt);
      });
      wardSelect.disabled = false;
    }

    updateFullShippingAddress();
  }
}

/**
 * 3. Character Counter for orders.note (max 255 chars)
 */
function initOrderNoteCounter() {
  const noteArea = document.getElementById("orderNote");
  const counter = document.getElementById("noteCharCount");

  if (!noteArea || !counter) return;

  function updateCount() {
    const len = noteArea.value.length;
    counter.textContent = `${len}/255 ký tự`;
    if (len >= 250) {
      counter.style.color = "#d70015";
    } else {
      counter.style.color = "var(--apple-ink-muted-48)";
    }
  }

  noteArea.addEventListener("input", updateCount);
  updateCount();
}

/**
 * 4. VAT Invoice Drawer Toggle
 */
function initVatToggle() {
  const vatCheckbox = document.getElementById("needVatInvoice");
  const vatDrawer = document.getElementById("vatFieldsDrawer");

  if (!vatCheckbox || !vatDrawer) return;

  vatCheckbox.addEventListener("change", function () {
    if (this.checked) {
      vatDrawer.classList.add("open");
    } else {
      vatDrawer.classList.remove("open");
    }
  });
}

/**
 * 5. Shipping Options Selector
 */
function initShippingOptions() {
  const shippingItems = document.querySelectorAll(".shipping-option-item");

  shippingItems.forEach((item) => {
    item.addEventListener("click", function () {
      const radio = item.querySelector('input[type="radio"]');
      if (radio) {
        radio.checked = true;
      }
      shippingItems.forEach((i) => i.classList.remove("selected"));
      item.classList.add("selected");
    });
  });
}

/**
 * 6. Promo Code (e.g. APPLEVIP)
 */
function initPromoCode() {
  const promoToggle = document.getElementById("promoToggleBtn");
  const promoBox = document.getElementById("promoBox");
  const promoInput = document.getElementById("promoCodeInput");
  const promoApplyBtn = document.getElementById("promoApplyBtn");
  const discountRow = document.getElementById("discountRow");
  const discountVal = document.getElementById("discountVal");
  const totalVal = document.getElementById("checkoutTotalDisplay");

  if (!promoToggle || !promoBox || !promoApplyBtn) return;

  promoToggle.addEventListener("click", function () {
    promoBox.classList.toggle("active");
  });

  promoApplyBtn.addEventListener("click", function () {
    const code = (promoInput.value || "").trim().toUpperCase();
    if (code === "APPLEVIP") {
      discountRow.classList.remove("d-none");
      discountVal.textContent = "-500.000đ";
      totalVal.textContent = "32.919.000đ";
      promoInput.disabled = true;
      promoApplyBtn.textContent = "Đã áp dụng";
      promoApplyBtn.style.backgroundColor = "#34c759";
      alert("Áp dụng mã ưu đãi APPLEVIP thành công! Giảm 500.000đ trực tiếp vào đơn hàng.");
    } else if (code === "") {
      alert("Vui lòng nhập mã giảm giá.");
    } else {
      alert("Mã giảm giá không hợp lệ hoặc đã hết hạn. Bạn có thể thử mã: APPLEVIP");
    }
  });
}

/**
 * 7. Client-side Form Validation
 */
function initFormValidation() {
  const form = document.getElementById("checkoutForm");
  if (!form) return;

  form.addEventListener("submit", function (e) {
    let isValid = true;

    // Validate Họ tên
    const nameInput = document.getElementById("recipientName");
    if (!nameInput.value.trim()) {
      showError(nameInput, "Vui lòng nhập họ và tên người nhận");
      isValid = false;
    } else {
      clearError(nameInput);
    }

    // Validate Điện thoại
    const phoneInput = document.getElementById("shippingPhone");
    const phoneRegex = /(84|0[3|5|7|8|9])+([0-9]{8})\b/;
    if (!phoneInput.value.trim() || !phoneRegex.test(phoneInput.value.trim())) {
      showError(phoneInput, "Vui lòng nhập số điện thoại hợp lệ (10 số)");
      isValid = false;
    } else {
      clearError(phoneInput);
    }

    // Validate Email
    const emailInput = document.getElementById("email");
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailInput.value.trim() || !emailRegex.test(emailInput.value.trim())) {
      showError(emailInput, "Vui lòng nhập địa chỉ email hợp lệ");
      isValid = false;
    } else {
      clearError(emailInput);
    }

    // Validate Địa chỉ cụ thể
    const streetInput = document.getElementById("streetAddress");
    if (!streetInput.value.trim()) {
      showError(streetInput, "Vui lòng nhập số nhà, tên đường hoặc toà nhà");
      isValid = false;
    } else {
      clearError(streetInput);
    }

    // Ensure full shipping address is assembled
    const fullAddressInput = document.getElementById("shippingAddress");
    if (fullAddressInput && !fullAddressInput.value.trim()) {
      const parts = [
        streetInput.value.trim(),
        document.getElementById("wardSelect").value,
        document.getElementById("districtSelect").value,
        document.getElementById("provinceSelect").value
      ].filter(Boolean);
      fullAddressInput.value = parts.join(", ");
    }

    if (!isValid) {
      e.preventDefault();
      // Scroll to the first error input
      const firstError = document.querySelector(".is-invalid");
      if (firstError) {
        firstError.scrollIntoView({ behavior: "smooth", block: "center" });
        firstError.focus();
      }
    }
  });

  function showError(input, msg) {
    input.classList.add("is-invalid");
    const feedback = input.parentElement.parentElement.querySelector(".apple-invalid-feedback");
    if (feedback) feedback.textContent = msg;
  }

  function clearError(input) {
    input.classList.remove("is-invalid");
  }
}

