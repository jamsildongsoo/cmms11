package com.cmms11.web;

import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class RoleController {

    @GetMapping("/domain/role/list")
    public String listForm(@RequestParam(name = "q", required = false) String q, Pageable pageable, Model model) {
        model.addAttribute("page", Page.empty());
        model.addAttribute("keyword", q);
        model.addAttribute("ready", Boolean.TRUE);
        return "domain/role/list";
    }

    @GetMapping("/domain/role/form")
    public String newForm(Model model) {
        model.addAttribute("roleId", null);
        model.addAttribute("isNew", true);
        return "domain/role/form";
    }

    @GetMapping("/domain/role/edit/{roleId}")
    public String editForm(@PathVariable String roleId, Model model) {
        model.addAttribute("roleId", roleId);
        model.addAttribute("isNew", false);
        return "domain/role/form";
    }

    @PostMapping("/domain/role/save")
    public String save() {
        throw new UnsupportedOperationException("Role management is not implemented yet");
    }

    @PostMapping("/domain/role/delete/{roleId}")
    public String deleteForm(@PathVariable String roleId) {
        throw new UnsupportedOperationException("Role management is not implemented yet");
    }

    @ResponseBody
    @GetMapping("/api/domain/roles")
    public ResponseEntity<?> list(@RequestParam(name = "q", required = false) String q, Pageable pageable) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
            .body(Map.of("message", "list roles not implemented"));
    }

    @ResponseBody
    @GetMapping("/api/domain/roles/{roleId}")
    public ResponseEntity<?> get(@PathVariable("roleId") String roleId) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
            .body(Map.of("message", "get role not implemented"));
    }

    @ResponseBody
    @PostMapping("/api/domain/roles")
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
            .body(Map.of("message", "create role not implemented"));
    }

    @ResponseBody
    @PutMapping("/api/domain/roles/{roleId}")
    public ResponseEntity<?> update(@PathVariable("roleId") String roleId, @RequestBody Map<String, Object> body) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
            .body(Map.of("message", "update role not implemented"));
    }

    @ResponseBody
    @DeleteMapping("/api/domain/roles/{roleId}")
    public ResponseEntity<?> delete(@PathVariable("roleId") String roleId) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
            .body(Map.of("message", "delete role not implemented"));
    }
}
