import React, { useState, useEffect } from "react";
import { Plus, Pencil, Trash2, X, Image as ImageIcon, Layers, Filter, ChevronDown, ChevronUp, ChevronLeft, ChevronRight } from "lucide-react";
import { ToastContainer, toast } from "react-toastify";
import 'react-toastify/dist/ReactToastify.css';
import { request } from "../../../untils/request";

// Mock data for sizes (replace with real data from API if available)
const mockSizes = [
  { SizeId: 1, SizeName: "S" },
  { SizeId: 2, SizeName: "M" },
  { SizeId: 3, SizeName: "L" },
  { SizeId: 4, SizeName: "XL" },
  { SizeId: 5, SizeName: "XXL" },
];

// Hàm map tên size sang id
function getSizeIdByName(name) {
  // Đổi từ sizeList sang mockSizes
  const found = mockSizes.find(s => s.SizeName === name);
  return found ? found.SizeId : null;
}

// Hàm map 1 sản phẩm từ backend sang front end
function mapProduct(item) {
  return {
    ProductId: item.id,
    Name: item.name,
    Price: item.price,
    Quantity: item.quantity,
    Description: item.description || "",
    CategoryId: item.categoryId || 1, // nếu có
    CategoryName: item.category || "", // Lấy đúng tên thể loại từ API
    Images: item.images
      ? item.images.map((imgId) => ({
        ImageId: imgId,
        Image: imgId.includes(".")
          ? `http://localhost:8080/images/${imgId}`
          : `http://localhost:8080/images/${imgId}.png`
      }))
      : [],
    Sizes: item.productSizeDTOS && Array.isArray(item.productSizeDTOS)
      ? item.productSizeDTOS.map(size => ({
        sizeId: getSizeIdByName(size.sizeName),
        quantity: size.quantity || 0
      })).filter(s => s.sizeId)
      : [],
    Status: item.quantity > 0 ? "Còn hàng" : "Hết hàng"
  };
}

function formatPrice(price) {
  return price.toLocaleString("vi-VN") + " đ";
}

function ProductManagement() {
  // State
  const [products, setProducts] = useState([]);
  const [categories, setCategories] = useState([]);
  const [sizes] = useState(mockSizes);
  const [search, setSearch] = useState("");
  const [filter, setFilter] = useState({
    category: "",
    status: "",
    priceMin: "",
    priceMax: "",
  });
  const [showFilter, setShowFilter] = useState(false);

  // Pagination state
  const [page, setPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [totalItems, setTotalItems] = useState(0);

  // Lấy token từ localStorage (hoặc nơi bạn lưu)
  const token = localStorage.getItem("token");

  //api 
  useEffect(() => {
    const params = {};
    // Nếu filter.category là rỗng, không truyền categoryId => lấy tất cả sản phẩm
    if (filter.category && !isNaN(Number(filter.category))) {
      params.categoryId = filter.category;
    }
    if (filter.priceMin) params.minPrice = filter.priceMin;
    if (filter.priceMax) params.maxPrice = filter.priceMax;
    if (search) params.name = search;
    params.page = page - 1; // Nếu backend phân trang từ 0
    params.size = 20;

    request.get("products/search", {
      params,
      headers: {
        Authorization: `Bearer ${token}`
      }
    })
      .then(res => {
        const result = res.data && res.data.result;
        console.log("Kết quả tìm kiếm sản phẩm:", result);
        if (res.data && res.data.code === 1000 && result && Array.isArray(result.content)) {
          setProducts(result.content.map(mapProduct));
          setTotalPages(result.totalPages > 0 ? result.totalPages : 1);
          setTotalItems(
            typeof result.totalElements === "number"
              ? result.totalElements
              : (result.content.length || 0)
          );
        } else if (Array.isArray(result)) {
          setProducts(result.map(mapProduct));
          setTotalPages(1);
          setTotalItems(result.length);
        } else {
          setProducts([]);
          setTotalPages(1);
          setTotalItems(0);
        }
      })
      .catch(err => {
        console.error("Lỗi khi lấy sản phẩm:", err);
      });
  }, [filter.category, filter.status, filter.priceMin, filter.priceMax, search, page]);
  useEffect(() => {
    request.get("category/getAll", {
      headers: {
        Authorization: `Bearer ${token}`
      }
    })
      .then(res => {
        if (res.data && res.data.code === 1000 && Array.isArray(res.data.result)) {
          setCategories(
            res.data.result.map(item => ({
              CategoryId: item.categoryId,
              Name: item.categoryName
            }))
          );
        }
      })
      .catch(err => {
        console.error("Lỗi khi lấy danh sách thể loại:", err);
      });
  }, []);

  const [showModal, setShowModal] = useState(false);
  const [modalType, setModalType] = useState("add"); // add | edit
  const [selectedProduct, setSelectedProduct] = useState(null);

  // Product form state
  const [form, setForm] = useState({
    Name: "",
    Price: "",
    Quantity: "",
    Description: "",
    CategoryId: "",
    Images: [],
    Sizes: [],
    soldCount: ""
  });

  // Image management
  const [imageFiles, setImageFiles] = useState([]); // Ảnh mới chọn (File)
  const [imagePreviews, setImagePreviews] = useState([]); // Preview cho cả ảnh cũ và mới

  // Size management
  const handleSizeToggle = (sizeId) => {
    setForm((prev) => {
      const exists = prev.Sizes.find(s => s.sizeId === sizeId);
      let newSizes;
      if (exists) {
        newSizes = prev.Sizes.filter((s) => s.sizeId !== sizeId);
      } else {
        newSizes = [...prev.Sizes, { sizeId, quantity: 0 }];
      }
      // Auto calc quantity
      const totalQty = newSizes.reduce((sum, item) => sum + (Number(item.quantity) || 0), 0);
      return { ...prev, Sizes: newSizes, Quantity: totalQty };
    });
  };

  const handleSizeQuantityChange = (sizeId, qty) => {
    setForm((prev) => {
      const newSizes = prev.Sizes.map(s =>
        s.sizeId === sizeId ? { ...s, quantity: Number(qty) } : s
      );
      const totalQty = newSizes.reduce((sum, item) => sum + (Number(item.quantity) || 0), 0);
      return { ...prev, Sizes: newSizes, Quantity: totalQty };
    });
  };

  // Open modal for add/edit
  const openModal = (type, product = null) => {
    setModalType(type);
    setShowModal(true);
    if (type === "edit" && product) {
      setSelectedProduct(product);
      setForm({
        Name: product.Name,
        Price: product.Price,
        Quantity: product.Quantity,
        Description: product.Description,
        CategoryId: product.CategoryId,
        Images: product.Images || [],
        Sizes: product.Sizes || [],
      });
      // Preview gồm ảnh cũ (url) + ảnh mới (nếu có)
      setImagePreviews((product.Images || []).map(img => img.Image));
      setImageFiles([]); // reset file mới
    } else {
      setSelectedProduct(null);
      setForm({
        Name: "",
        Price: "",
        Quantity: "",
        Description: "",
        CategoryId: "",
        Images: [],
        Sizes: [],
      });
      setImageFiles([]);
      setImagePreviews([]);
    }
  };

  // Close modal
  const closeModal = () => {
    setShowModal(false);
    setSelectedProduct(null);
    setForm({
      Name: "",
      Price: "",
      Quantity: "",
      Description: "",
      CategoryId: "",
      Images: [],
      Sizes: [],

    });
    setImageFiles([]);
    setImagePreviews([]);
  };

  // Khi chọn ảnh mới, thêm vào danh sách preview (không ghi đè ảnh cũ)
  const handleImageChange = (e) => {
    const files = Array.from(e.target.files);
    setImageFiles(prev => [...prev, ...files]);
    setImagePreviews(prev => [
      ...prev,
      ...files.map(file => URL.createObjectURL(file))
    ]);
  };

  // Xóa ảnh ở vị trí idx (có thể là ảnh cũ hoặc mới)
  const handleRemoveImage = (idx) => {
    // Nếu là ảnh cũ (trong form.Images), xóa khỏi form.Images và imagePreviews
    if (idx < form.Images.length) {
      setForm(prev => ({
        ...prev,
        Images: prev.Images.filter((_, i) => i !== idx)
      }));
      setImagePreviews(prev => prev.filter((_, i) => i !== idx));
    } else {
      // Nếu là ảnh mới, xóa khỏi imageFiles và imagePreviews
      const fileIdx = idx - form.Images.length;
      setImageFiles(prev => prev.filter((_, i) => i !== fileIdx));
      setImagePreviews(prev => prev.filter((_, i) => i !== idx));
    }
  };

  // Add or update product
  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!form.Description || !form.CategoryId || !form.Name || !form.Price || !form.Quantity) {
      toast.error("Vui lòng điền đầy đủ thông tin");
      return;
    }
    let dataToSend;
    let config = { headers: { "Content-Type": "multipart/form-data", Authorization: `Bearer ${token}` } };
    if (modalType === "add") {
      const payload = {
        name: form.Name,
        price: Number(form.Price),
        quantity: Number(form.Quantity), // Auto calculated
        description: form.Description,
        categoryId: Number(form.CategoryId),
        productSizes: form.Sizes, // Send array of {sizeId, quantity}
      };
      const formData = new FormData();
      formData.append("product", JSON.stringify(payload));
      imageFiles.forEach(file => formData.append("images", file));
      dataToSend = formData;
      try {
        const res = await request.post("/products", dataToSend, config);
        if (res.data && res.data.code === 1000) {
          toast.success("Thêm sản phẩm thành công");
        } else {
          toast.error("Thêm sản phẩm thất bại");
          return;
        }
      } catch (err) {
        toast.error("Có lỗi xảy ra khi lưu sản phẩm");
        console.error(err);
        return;
      }
    } else {
      if (!selectedProduct || !selectedProduct.ProductId) {
        toast.error("Không xác định được sản phẩm để cập nhật!");
        return;
      }
      // Lấy tên ảnh cũ còn lại (sau khi xóa preview)
      const oldImageNames = (form.Images || []).map(img => {
        const parts = img.Image.split("/");
        return parts[parts.length - 1]; // Keep full name (e.g., uuid.webp or uuid.png)
      });
      const payload = {
        name: form.Name,
        price: Number(form.Price),
        quantity: Number(form.Quantity),
        description: form.Description,
        categoryId: Number(form.CategoryId),
        productSizes: form.Sizes,
        oldImageNames: oldImageNames,
      };
      const formData = new FormData();
      formData.append("product", JSON.stringify(payload));
      imageFiles.forEach(file => formData.append("images", file));
      try {
        const res = await request.put(`products/${selectedProduct.ProductId}`, formData, config);
        if (res.data && res.data.code === 1000) {
          toast.success("Cập nhật sản phẩm thành công");
        } else {
          toast.error("Cập nhật sản phẩm thất bại");
          return;
        }
      } catch (err) {
        toast.error("Có lỗi xảy ra khi lưu sản phẩm");
        console.error(err);
        return;
      }
    }
    closeModal();
    const params = {};
    if (filter.category && !isNaN(Number(filter.category))) {
      params.categoryId = filter.category;
    }
    if (filter.priceMin) params.minPrice = filter.priceMin;
    if (filter.priceMax) params.maxPrice = filter.priceMax;
    if (search) params.name = search;
    params.page = page - 1;
    params.size = 20;
    try {
      const res = await request.get("products/search", {
        params,
        headers: {
          Authorization: `Bearer ${token}`
        }
      });
      const result = res.data && res.data.result;
      if (res.data && res.data.code === 1000 && result && Array.isArray(result.content)) {
        setProducts(result.content.map(mapProduct));
        setTotalPages(result.totalPages > 0 ? result.totalPages : 1);
        setTotalItems(
          typeof result.totalElements === "number"
            ? result.totalElements
            : (result.content.length || 0)
        );
      } else if (Array.isArray(result)) {
        setProducts(result.map(mapProduct));
        setTotalPages(1);
        setTotalItems(result.length);
      } else {
        setProducts([]);
        setTotalPages(1);
        setTotalItems(0);
      }
    } catch (err) {
      // ignore
    }
  };

  // Delete product
  const handleDelete = (productId) => {
    if (window.confirm("Bạn có chắc chắn muốn xóa sản phẩm này?")) {
      request.delete(`products/${productId}`)
        .then(() => {
          setProducts(products.filter(p => p.ProductId !== productId));
          alert("Xóa sản phẩm thành công!");
        })
        .catch(err => {
          console.error(err);
          alert("Xóa thất bại!");
        });
    }
  };

  // Popup state
  const [showImagePopup, setShowImagePopup] = useState(false);
  const [popupImages, setPopupImages] = useState([]);
  const [popupIndex, setPopupIndex] = useState(0);

  // Open image popup
  const handleOpenImagePopup = (images, idx = 0) => {
    setPopupImages(images);
    setPopupIndex(idx);
    setShowImagePopup(true);
  };

  // Close image popup
  const handleCloseImagePopup = () => {
    setShowImagePopup(false);
    setPopupImages([]);
    setPopupIndex(0);
  };

  // Next/Prev image in popup
  const handlePrevImage = () => {
    setPopupIndex((prev) =>
      popupImages.length === 0 ? 0 : (prev === 0 ? popupImages.length - 1 : prev - 1)
    );
  };
  const handleNextImage = () => {
    setPopupIndex((prev) =>
      popupImages.length === 0 ? 0 : (prev === popupImages.length - 1 ? 0 : prev + 1)
    );
  };

  // Slide state for product detail
  const [showDetail, setShowDetail] = useState(false);
  const [detailProduct, setDetailProduct] = useState(null);
  const [detailImageIdx, setDetailImageIdx] = useState(0);
  const [expandedRow, setExpandedRow] = useState(null);

  // Open product detail slide
  const handleOpenDetail = (product) => {
    setDetailProduct(product);
    setDetailImageIdx(0);
    setShowDetail(true);
  };

  // Close product detail slide
  const handleCloseDetail = () => {
    setShowDetail(false);
    setDetailProduct(null);
    setDetailImageIdx(0);
  };

  // Next/Prev image in detail slide
  const handleDetailPrevImage = () => {
    if (!detailProduct?.Images?.length) return;
    setDetailImageIdx((prev) =>
      prev === 0 ? detailProduct.Images.length - 1 : prev - 1
    );
  };
  const handleDetailNextImage = () => {
    if (!detailProduct?.Images?.length) return;
    setDetailImageIdx((prev) =>
      prev === detailProduct.Images.length - 1 ? 0 : prev + 1
    );
  };

  // Khi chọn bộ lọc, reset lại page về 1
  useEffect(() => {
    setPage(1);
  }, [filter, search]);

  // Render
  return (
    <div>
      <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4 mb-6">
        <h2 className="text-2xl font-bold">Quản lý sản phẩm</h2>
        <button
          className="flex items-center bg-indigo-600 text-white px-4 py-2 rounded-lg hover:bg-indigo-700 transition"
          onClick={() => openModal("add")}
        >
          <Plus size={18} className="mr-2" /> Thêm sản phẩm
        </button>
      </div>

      {/* Filter/Search */}
      <div className="flex flex-col md:flex-row md:items-center gap-4 mb-4">
        <div className="relative w-full md:w-1/3">
          <input
            type="text"
            placeholder="Tìm kiếm theo tên sản phẩm..."
            value={search}
            onChange={e => setSearch(e.target.value)}
            className="w-full pl-10 pr-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500 bg-indigo-50"
          />
          <span className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400">
            <Filter size={18} />
          </span>
        </div>
        <button
          className="flex items-center gap-1 px-3 py-2 rounded-lg border border-indigo-200 text-indigo-700 hover:bg-indigo-50 transition"
          onClick={() => setShowFilter(v => !v)}
        >
          <Layers size={16} />
          Bộ lọc nâng cao
          {showFilter ? <ChevronUp size={16} /> : <ChevronDown size={16} />}
        </button>
      </div>
      {showFilter && (
        <div className="bg-indigo-50 rounded-lg p-4 mb-4 flex flex-wrap gap-4 items-center">
          <div>
            <label className="block text-xs font-medium mb-1">Thể loại</label>
            <select
              value={filter.category}
              onChange={e => setFilter(f => ({ ...f, category: e.target.value }))
              }
              className="border rounded px-2 py-1"
            >
              <option value="">Tất cả</option>
              {categories.map(c => (
                <option key={c.CategoryId} value={c.CategoryId}>{c.Name}</option>
              ))}
            </select>
          </div>
          <div>
            <label className="block text-xs font-medium mb-1">Trạng thái</label>
            <select
              value={filter.status}
              onChange={e => setFilter(f => ({ ...f, status: e.target.value }))}
              className="border rounded px-2 py-1"
            >
              <option value="">Tất cả</option>
              <option value="Còn hàng">Còn hàng</option>
              <option value="Hết hàng">Hết hàng</option>
            </select>
          </div>
          <div>
            <label className="block text-xs font-medium mb-1">Giá từ</label>
            <input
              type="number"
              value={filter.priceMin}
              onChange={e => setFilter(f => ({ ...f, priceMin: e.target.value }))}
              className="border rounded px-2 py-1 w-24"
              min={0}
            />
          </div>
          <div>
            <label className="block text-xs font-medium mb-1">Đến</label>
            <input
              type="number"
              value={filter.priceMax}
              onChange={e => setFilter(f => ({ ...f, priceMax: e.target.value }))}
              className="border rounded px-2 py-1 w-24"
              min={0}
            />
          </div>
        </div>
      )}
      {/* Product Table */}
      <div className="overflow-x-auto bg-white shadow-lg rounded-xl">
        <table className="min-w-full text-sm text-left">
          <thead className="bg-indigo-50 text-indigo-700">
            <tr>
              <th className="p-3 font-semibold">Ảnh</th>
              <th className="p-3 font-semibold">Tên sản phẩm</th>
              <th className="p-3 font-semibold">Giá</th>
              <th className="p-3 font-semibold">Số lượng</th>
              <th className="p-3 font-semibold">Thể loại</th>
              <th className="p-3 font-semibold">Trạng thái</th>
              <th className="p-3 font-semibold">Size</th>
              <th className="p-3 text-center font-semibold">Hành động</th>
            </tr>
          </thead>
          <tbody>
            {products.map((product, idx) => (
              <React.Fragment key={product.ProductId || idx}>
                <tr
                  className={`border-t hover:bg-indigo-50 transition cursor-pointer`}
                >
                  <td className="p-3">
                    {product.Images && product.Images.length > 0 ? (
                      <img
                        src={product.Images[0].Image}
                        alt={product.Name}
                        className="w-16 h-16 object-cover rounded shadow cursor-pointer"
                        title="Xem ảnh sản phẩm"
                        onClick={e => {
                          e.stopPropagation();
                          handleOpenImagePopup(product.Images.map(img => img.Image));
                        }}
                      />
                    ) : (
                      <span className="text-gray-400"><ImageIcon size={32} /></span>
                    )}
                  </td>
                  <td className="p-3">{product.Name}</td>
                  <td className="p-3">{formatPrice(product.Price)}</td>
                  <td className="p-3">{product.Quantity}</td>
                  <td className="p-3">{product.CategoryName}</td>
                  <td className="p-3">
                    <span className={`px-2 py-1 rounded-full text-xs font-semibold
                      ${product.Status === "Còn hàng" ? "bg-green-100 text-green-700" : "bg-red-100 text-red-700"}`}>
                      {product.Status}
                    </span>
                  </td>
                  <td className="p-3">
                    {product.Sizes && product.Sizes.length > 0
                      ? product.Sizes.map(s => {
                        const sizeName = mockSizes.find(m => m.SizeId === s.sizeId)?.SizeName;
                        return `${sizeName} (${s.quantity})`;
                      }).join(", ")
                      : <span className="text-gray-400">-</span>
                    }
                  </td>
                  <td className="p-3 flex justify-center space-x-2">
                    <button
                      className="text-indigo-600 hover:text-indigo-800 p-2 rounded-full hover:bg-indigo-100 transition"
                      title="Sửa"
                      onClick={e => { e.stopPropagation(); openModal("edit", product); }}
                    >
                      <Pencil size={18} />
                    </button>
                    <button
                      className="text-red-600 hover:text-red-800 p-2 rounded-full hover:bg-red-100 transition"
                      title="Xóa"
                      onClick={e => { e.stopPropagation(); handleDelete(product.ProductId); }}
                    >
                      <Trash2 size={18} />
                    </button>
                  </td>
                </tr>
              </React.Fragment>
            ))}
            {products.length === 0 && (
              <tr>
                <td colSpan="8" className="text-center p-4 text-gray-500">Không tìm thấy sản phẩm.</td>
              </tr>
            )}
          </tbody>
        </table>
        {/* Pagination UI */}
        <div className="flex justify-between items-center px-4 py-3">
          <div className="text-sm text-gray-500">
            Tổng: {totalItems} sản phẩm | Trang {page}/{totalPages}
          </div>
          <div className="flex gap-1">
            <button
              className="px-2 py-1 text-xs rounded border bg-white hover:bg-indigo-50 disabled:opacity-50"
              onClick={() => setPage(p => Math.max(1, p - 1))}
              disabled={page <= 1}
            >
              &lt;
            </button>
            {[...Array(totalPages)].map((_, idx) => (
              <button
                key={idx + 1}
                className={`px-2 py-1 text-xs rounded border ${page === idx + 1 ? "bg-indigo-600 text-white" : "bg-white hover:bg-indigo-50"}`}
                onClick={() => setPage(idx + 1)}
                disabled={page === idx + 1}
              >
                {idx + 1}
              </button>
            ))}
            <button
              className="px-2 py-1 text-xs rounded border bg-white hover:bg-indigo-50 disabled:opacity-50"
              onClick={() => setPage(p => Math.min(totalPages, p + 1))}
              disabled={page >= totalPages}
            >
              &gt;
            </button>
          </div>
        </div>
      </div>

      {/* Modal Thêm/Sửa sản phẩm */}
      {showModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center  bg-opacity-10 backdrop-blur-[2px] transition-all">
          <div className="bg-white rounded-xl shadow-2xl w-full max-w-2xl p-6 relative animate-fade-in">
            <button
              className="absolute top-3 right-3 text-gray-400 hover:text-red-500"
              onClick={closeModal}
              aria-label="Đóng"
            >
              <X size={22} />
            </button>
            <h3 className="text-xl font-bold mb-4 flex items-center gap-2">
              {modalType === "add" ? <Plus size={22} /> : <Pencil size={20} />}
              {modalType === "add" ? "Thêm sản phẩm" : "Chỉnh sửa sản phẩm"}
            </h3>
            <form onSubmit={handleSubmit} className="space-y-4" autoComplete="off">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium mb-1">Tên sản phẩm</label>
                  <input
                    type="text"
                    required
                    value={form.Name}
                    onChange={e => setForm(f => ({ ...f, Name: e.target.value }))}
                    className="w-full border rounded-lg px-3 py-2 focus:ring-2 focus:ring-indigo-500 bg-indigo-50"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium mb-1">Giá</label>
                  <input
                    type="number"
                    required
                    min={0}
                    value={form.Price}
                    onChange={e => setForm(f => ({ ...f, Price: e.target.value }))}
                    className="w-full border rounded-lg px-3 py-2 focus:ring-2 focus:ring-indigo-500 bg-indigo-50"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium mb-1">Số lượng</label>
                  <input
                    type="number"
                    required
                    min={0}
                    value={form.Quantity}
                    readOnly
                    className="w-full border rounded-lg px-3 py-2 bg-gray-100 focus:ring-2 focus:ring-indigo-500 cursor-not-allowed"
                    title="Tự động tính tổng từ số lượng các size"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium mb-1">Thể loại</label>
                  <select
                    required
                    value={form.CategoryId}
                    onChange={e => setForm(f => ({ ...f, CategoryId: e.target.value }))}
                    className="w-full border rounded-lg px-3 py-2 focus:ring-2 focus:ring-indigo-500 bg-indigo-50"
                  >
                    <option value="">Thể loại</option>
                    {categories.map(c => (
                      <option key={c.CategoryId} value={c.CategoryId}>{c.Name}</option>
                    ))}
                  </select>
                </div>
              </div>
              <div>
                <label className="block text-sm font-medium mb-1">Mô tả</label>
                <textarea
                  value={form.Description}
                  onChange={e => setForm(f => ({ ...f, Description: e.target.value }))}
                  className="w-full border rounded-lg px-3 py-2 focus:ring-2 focus:ring-indigo-500 bg-indigo-50"
                  rows={2}
                />
              </div>
              {/* Image management */}
              <div>
                <label className="block text-sm font-medium mb-1">Ảnh sản phẩm</label>
                <input
                  type="file"
                  accept="image/*"
                  multiple
                  onChange={handleImageChange}
                  className="mb-2"
                />
                <div className="flex flex-wrap gap-2">
                  {imagePreviews.map((img, idx) => (
                    <div key={idx} className="relative">
                      <img src={img} alt="preview" className="w-16 h-16 object-cover rounded border" />
                      <button
                        type="button"
                        className="absolute -top-2 -right-2 bg-red-500 text-white rounded-full p-1"
                        onClick={() => handleRemoveImage(idx)}
                        title="Xóa ảnh"
                      >
                        <X size={14} />
                      </button>
                    </div>
                  ))}
                </div>
              </div>

              <div>
                <label className="block text-sm font-medium mb-1">Size</label>
                <div className="flex flex-col gap-2">
                  {sizes.map(size => {
                    const isChecked = form.Sizes.some(s => s.sizeId === size.SizeId);
                    const currentQty = form.Sizes.find(s => s.sizeId === size.SizeId)?.quantity || "";

                    return (
                      <div key={size.SizeId} className="flex items-center gap-4">
                        <label className="flex items-center gap-1 cursor-pointer w-20">
                          <input
                            type="checkbox"
                            checked={isChecked}
                            onChange={() => handleSizeToggle(size.SizeId)}
                            className="accent-indigo-600"
                          />
                          <span className="text-sm">{size.SizeName}</span>
                        </label>
                        {isChecked && (
                          <input
                            type="number"
                            placeholder="Số lượng"
                            className="border rounded px-2 py-1 w-24 text-sm"
                            min="0"
                            value={currentQty}
                            onChange={(e) => handleSizeQuantityChange(size.SizeId, e.target.value)}
                          />
                        )}
                      </div>
                    )
                  })}
                </div>
              </div>
              <div className="flex justify-end gap-2 pt-2">
                <button
                  type="button"
                  className="px-4 py-2 rounded-lg bg-gray-100 text-gray-700 hover:bg-gray-200 transition"
                  onClick={closeModal}
                >
                  Hủy
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 rounded-lg bg-indigo-600 text-white hover:bg-indigo-700 transition font-semibold"
                  tabIndex={0}
                  disabled={false}
                >
                  {modalType === "add" ? "Thêm" : "Lưu"}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {showDetail && detailProduct && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-opacity-60 transition-all">
          <div className="relative bg-white rounded-2xl shadow-2xl w-full max-w-2xl p-0 flex flex-col md:flex-row overflow-hidden">
            {/* Ảnh slide */}
            <div className="flex flex-col items-center justify-center bg-indigo-50 p-6 md:w-1/2 min-h-[340px]">
              <div className="flex items-center gap-3">
                <button
                  className="p-2 rounded-full bg-gray-100 hover:bg-indigo-100 text-indigo-600 transition"
                  onClick={handleDetailPrevImage}
                  aria-label="Trước"
                >
                  <ChevronLeft size={28} />
                </button>
                <img
                  src={detailProduct.Images[detailImageIdx]?.Image}
                  alt="Ảnh sản phẩm"
                  className="max-h-[260px] max-w-[220px] rounded-lg shadow border bg-white"
                  style={{ objectFit: "contain" }}
                />
                <button
                  className="p-2 rounded-full bg-gray-100 hover:bg-indigo-100 text-indigo-600 transition"
                  onClick={handleDetailNextImage}
                  aria-label="Sau"
                >
                  <ChevronRight size={28} />
                </button>
              </div>
              <div className="flex gap-2 mt-3">
                {detailProduct.Images.map((img, idx) => (
                  <img
                    key={idx}
                    src={img.Image}
                    alt="thumb"
                    className={`w-10 h-10 object-cover rounded border cursor-pointer ${idx === detailImageIdx ? "ring-2 ring-indigo-500" : ""}`}
                    onClick={() => setDetailImageIdx(idx)}
                  />
                ))}
              </div>
            </div>
            {/* Thông tin sản phẩm */}
            <div className="flex-1 p-6 flex flex-col justify-between">
              <button
                className="absolute top-3 right-3 text-gray-400 hover:text-red-500"
                onClick={handleCloseDetail}
                aria-label="Đóng"
              >
                <X size={24} />
              </button>
              <div>
                <h3 className="text-2xl font-bold text-indigo-700 mb-2">{detailProduct.Name}</h3>
                <div className="mb-2 text-lg font-semibold text-indigo-600">{formatPrice(detailProduct.Price)}</div>
                <div className="mb-2">
                  <span className="font-medium">Thể loại: </span>
                  {detailProduct.CategoryName}
                </div>
                <div className="mb-2">
                  <span className="font-medium">Số lượng: </span>
                  {detailProduct.Quantity}
                </div>
                <div className="mb-2">
                  <span className="font-medium">Trạng thái: </span>
                  <span className={`px-2 py-1 rounded-full text-xs font-semibold
                    ${detailProduct.Status === "Còn hàng" ? "bg-green-100 text-green-700" : "bg-red-100 text-red-700"}`}>
                    {detailProduct.Status}
                  </span>
                </div>
                <div className="mb-2">
                  <span className="font-medium">Size: </span>
                  {detailProduct.Sizes && detailProduct.Sizes.length > 0
                    ? detailProduct.Sizes.map(sid => sizes.find(s => s.SizeId === sid)?.SizeName).join(", ")
                    : <span className="text-gray-400">-</span>
                  }
                </div>
                <div className="mb-2">
                  <span className="font-medium">Mô tả: </span>
                  <span>{detailProduct.Description}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Popup xem nhiều ảnh */}
      {showImagePopup && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-60 transition-all">
          <div className="relative bg-white rounded-xl shadow-2xl p-4 flex flex-col items-center max-w-lg w-full">
            <button
              className="absolute top-2 right-2 text-gray-400 hover:text-red-500"
              onClick={handleCloseImagePopup}
              aria-label="Đóng"
            >
              <X size={24} />
            </button>
            <div className="flex items-center gap-4 mt-6 mb-2">
              <button
                className="p-2 rounded-full bg-gray-100 hover:bg-indigo-100 text-indigo-600 transition"
                onClick={handlePrevImage}
                aria-label="Trước"
              >
                <ChevronLeft size={28} />
              </button>
              <img
                src={popupImages[popupIndex]}
                alt="Ảnh sản phẩm"
                className="max-h-[350px] max-w-[350px] rounded-lg shadow border"
                style={{ objectFit: "contain" }}
              />
              <button
                className="p-2 rounded-full bg-gray-100 hover:bg-indigo-100 text-indigo-600 transition"
                onClick={handleNextImage}
                aria-label="Sau"
              >
                <ChevronRight size={28} />
              </button>
            </div>
            <div className="flex gap-2 mt-2">
              {popupImages.map((img, idx) => (
                <img
                  key={idx}
                  src={img}
                  alt="thumb"
                  className={`w-12 h-12 object-cover rounded border cursor-pointer ${idx === popupIndex ? "ring-2 ring-indigo-500" : ""}`}
                  onClick={() => setPopupIndex(idx)}
                />
              ))}
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default ProductManagement;
