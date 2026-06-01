package com.smartcashier.web.controller;

import com.smartcashier.web.service.ReportService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/sales")
    public String sales(Model model) {
        model.addAttribute("transactions", reportService.getSalesReport());
        model.addAttribute("grandTotal", reportService.getSalesTotal());
        model.addAttribute("pageTitle", "Laporan Penjualan");
        model.addAttribute("activeNav", "reports-sales");
        return "reports/sales";
    }

    @GetMapping("/purchases")
    public String purchases(Model model) {
        model.addAttribute("transactions", reportService.getPurchaseReport());
        model.addAttribute("grandTotal", reportService.getPurchaseTotal());
        model.addAttribute("pageTitle", "Laporan Pembelian");
        model.addAttribute("activeNav", "reports-purchases");
        return "reports/purchases";
    }
}
