package com.cmms11.web;

import com.cmms11.domain.storage.StorageRequest;
import com.cmms11.domain.storage.StorageResponse;
import com.cmms11.domain.storage.StorageService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 창고 기준정보 화면 컨트롤러.
 */
@Controller
@RequestMapping("/domain/storage")
public class StorageViewController {

    private final StorageService service;

    public StorageViewController(StorageService service) {
        this.service = service;
    }

    @GetMapping("/list")
    public String list(
        @RequestParam(name = "q", required = false) String keyword,
        Pageable pageable,
        Model model
    ) {
        Page<StorageResponse> page = service.list(keyword, pageable);
        model.addAttribute("page", page);
        model.addAttribute("keyword", keyword);
        return "domain/storage/list";
    }

    @GetMapping("/form")
    public String newForm(Model model) {
        model.addAttribute("storage", emptyStorage());
        model.addAttribute("isNew", true);
        return "domain/storage/form";
    }

    @GetMapping("/edit/{storageId}")
    public String editForm(@PathVariable String storageId, Model model) {
        model.addAttribute("storage", service.get(storageId));
        model.addAttribute("isNew", false);
        return "domain/storage/form";
    }

    @PostMapping("/save")
    public String save(
        @RequestParam String storageId,
        @RequestParam String name,
        @RequestParam(name = "note", required = false) String note,
        @RequestParam(name = "isNew", required = false) String isNew
    ) {
        StorageRequest request = new StorageRequest(storageId, name, note);
        if ("true".equals(isNew)) {
            service.create(request);
        } else {
            service.update(storageId, request);
        }
        return "redirect:/domain/storage/list";
    }

    @PostMapping("/delete/{storageId}")
    public String delete(@PathVariable String storageId) {
        service.delete(storageId);
        return "redirect:/domain/storage/list";
    }

    private StorageResponse emptyStorage() {
        return new StorageResponse(
            null,
            null,
            null,
            "N",
            null,
            null,
            null,
            null
        );
    }
}

