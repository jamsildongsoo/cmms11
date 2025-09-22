package com.cmms11.web;

import com.cmms11.code.CodeItemRequest;
import com.cmms11.code.CodeItemResponse;
import com.cmms11.code.CodeService;
import com.cmms11.code.CodeTypeRequest;
import com.cmms11.code.CodeTypeResponse;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 공통 코드(타입/항목) 화면 컨트롤러.
 */
@Controller
@RequestMapping("/code")
public class CodeViewController {

    private final CodeService service;

    public CodeViewController(CodeService service) {
        this.service = service;
    }

    @GetMapping("/types")
    public String listTypes(
        @RequestParam(name = "q", required = false) String keyword,
        Pageable pageable,
        Model model
    ) {
        Page<CodeTypeResponse> page = service.listTypes(keyword, pageable);
        model.addAttribute("page", page);
        model.addAttribute("keyword", keyword);
        return "code/list";
    }

    @GetMapping({"/types/new", "/types/form"})
    public String newTypeForm(Model model) {
        model.addAttribute("type", emptyType());
        model.addAttribute("isNew", true);
        return "code/form";
    }

    @GetMapping("/types/{codeType}")
    public String editTypeForm(@PathVariable String codeType, Model model) {
        model.addAttribute("type", service.getType(codeType));
        model.addAttribute("isNew", false);
        return "code/form";
    }

    @PostMapping("/types/save")
    public String saveType(
        @RequestParam String codeType,
        @RequestParam String name,
        @RequestParam(name = "note", required = false) String note,
        @RequestParam(name = "isNew", required = false) String isNew
    ) {
        CodeTypeRequest request = new CodeTypeRequest(codeType, name, note);
        if ("true".equals(isNew)) {
            service.createType(request);
        } else {
            service.updateType(codeType, request);
        }
        return "redirect:/code/types";
    }

    @PostMapping("/types/delete/{codeType}")
    public String deleteType(@PathVariable String codeType) {
        service.deleteType(codeType);
        return "redirect:/code/types";
    }

    @GetMapping("/items")
    public String listItems(
        @RequestParam(name = "codeType", required = false) String codeType,
        @RequestParam(name = "q", required = false) String keyword,
        Pageable pageable,
        Model model
    ) {
        List<CodeTypeResponse> types = service.listTypes(null, PageRequest.of(0, 100)).getContent();
        model.addAttribute("codeTypes", types);
        model.addAttribute("selectedType", codeType);
        model.addAttribute("keyword", keyword);

        Page<CodeItemResponse> page = Page.empty(pageable);
        if (codeType != null && !codeType.isBlank()) {
            page = service.listItems(codeType, keyword, pageable);
        }
        model.addAttribute("page", page);
        return "code/item-list";
    }

    @GetMapping("/items/new")
    public String newItemForm(
        @RequestParam String codeType,
        Model model
    ) {
        model.addAttribute("type", service.getType(codeType));
        model.addAttribute("item", emptyItem(codeType));
        model.addAttribute("isNew", true);
        return "code/item-form";
    }

    @GetMapping("/items/{codeType}/{code}")
    public String editItemForm(
        @PathVariable String codeType,
        @PathVariable String code,
        Model model
    ) {
        model.addAttribute("type", service.getType(codeType));
        model.addAttribute("item", service.getItem(codeType, code));
        model.addAttribute("isNew", false);
        return "code/item-form";
    }

    @PostMapping("/items/save")
    public String saveItem(
        @RequestParam String codeType,
        @RequestParam String code,
        @RequestParam String name,
        @RequestParam(name = "note", required = false) String note,
        @RequestParam(name = "isNew", required = false) String isNew
    ) {
        CodeItemRequest request = new CodeItemRequest(codeType, code, name, note);
        if ("true".equals(isNew)) {
            service.createItem(request);
        } else {
            service.updateItem(codeType, code, request);
        }
        return "redirect:/code/items?codeType=" + codeType;
    }

    @PostMapping("/items/delete/{codeType}/{code}")
    public String deleteItem(@PathVariable String codeType, @PathVariable String code) {
        service.deleteItem(codeType, code);
        return "redirect:/code/items?codeType=" + codeType;
    }

    private CodeTypeResponse emptyType() {
        return new CodeTypeResponse(null, null, null, "N", null, null, null, null);
    }

    private CodeItemResponse emptyItem(String codeType) {
        return new CodeItemResponse(codeType, null, null, null);
    }
}

