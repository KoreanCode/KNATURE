package com.knature.bo.controller;

import com.knature.bo.service.ProductService;
import com.knature.common.domain.product.Product;
import com.knature.common.domain.product.ProductStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public String list(@RequestParam(required = false) String keyword,
                       @RequestParam(required = false) ProductStatus status,
                       @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
                       Model model) {
        Page<Product> products = productService.getProducts(keyword, status, pageable);
        model.addAttribute("products", products);
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);
        model.addAttribute("statuses", ProductStatus.values());
        return "product/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", productService.getAllCategories());
        model.addAttribute("statuses", ProductStatus.values());
        return "product/form";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("product", productService.getProduct(id));
        return "product/detail";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("product", productService.getProduct(id));
        model.addAttribute("categories", productService.getAllCategories());
        model.addAttribute("statuses", ProductStatus.values());
        return "product/form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute Product product, @RequestParam(required = false) Long categoryId,
                       RedirectAttributes ra) {
        if (categoryId != null) {
            product.setCategory(productService.getAllCategories().stream()
                    .filter(c -> c.getId().equals(categoryId)).findFirst().orElse(null));
        }
        productService.saveProduct(product);
        ra.addFlashAttribute("message", "상품이 저장되었습니다.");
        return "redirect:/admin/products";
    }

    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id, @RequestParam ProductStatus status,
                               RedirectAttributes ra) {
        productService.updateStatus(id, status);
        ra.addFlashAttribute("message", "상태가 변경되었습니다.");
        return "redirect:/admin/products";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        productService.deleteProduct(id);
        ra.addFlashAttribute("message", "상품이 삭제되었습니다.");
        return "redirect:/admin/products";
    }
}
