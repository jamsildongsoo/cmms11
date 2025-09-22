package com.cmms11.web;

import com.cmms11.domain.member.Member;
import com.cmms11.domain.member.MemberId;
import com.cmms11.domain.member.MemberService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.validation.annotation.Validated;

@Controller
@Validated
public class MemberController {
    private final MemberService service;

    public MemberController(MemberService service) {
        this.service = service;
    }

    @GetMapping("/domain/member/list")
    public String listForm(@RequestParam(name = "q", required = false) String q, Pageable pageable, Model model) {
        Page<Member> page = service.list(q, pageable);
        model.addAttribute("page", page);
        model.addAttribute("keyword", q);
        return "domain/member/list";
    }

    @GetMapping("/domain/member/form")
    public String newForm(Model model) {
        model.addAttribute("member", MemberForm.empty());
        model.addAttribute("isNew", true);
        return "domain/member/form";
    }

    @GetMapping("/domain/member/edit/{memberId}")
    public String editForm(@PathVariable String memberId, Model model) {
        Member member = service.get(memberId);
        model.addAttribute("member", MemberForm.from(member));
        model.addAttribute("isNew", false);
        return "domain/member/form";
    }

    @PostMapping("/domain/member/save")
    public String save(@ModelAttribute @Validated MemberForm form, @RequestParam(required = false) String isNew) {
        Member member = form.toEntity();
        if ("true".equals(isNew)) {
            service.create(member, form.getPassword(), null);
        } else {
            service.update(member, form.getPassword(), null);
        }
        return "redirect:/domain/member/list";
    }

    @PostMapping("/domain/member/delete/{memberId}")
    public String deleteForm(@PathVariable String memberId) {
        service.delete(memberId, null);
        return "redirect:/domain/member/list";
    }

    @ResponseBody
    @GetMapping("/api/members")
    public Page<Member> list(@RequestParam(name = "q", required = false) String q, Pageable pageable) {
        return service.list(q, pageable);
    }

    @ResponseBody
    @GetMapping("/api/members/{memberId}")
    public ResponseEntity<Member> get(@PathVariable String memberId) {
        return ResponseEntity.ok(service.get(memberId));
    }

    @ResponseBody
    @PostMapping("/api/members")
    public ResponseEntity<Member> create(@Valid @RequestBody MemberCreateRequest req) {
        Member member = new Member();
        member.setId(new MemberId(null, req.member_id));
        member.setName(req.name);
        member.setDeptId(req.dept_id);
        member.setEmail(req.email);
        member.setPhone(req.phone);
        member.setNote(req.note);
        if (req.delete_mark != null) {
            member.setDeleteMark(req.delete_mark);
        }
        Member saved = service.create(member, req.password, req.actor);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    public static class MemberForm {
        @NotBlank
        @Size(max = 5)
        private String memberId;

        @NotBlank
        @Size(max = 100)
        private String name;

        @Size(max = 5)
        private String deptId;

        @Size(max = 100)
        private String email;

        @Size(max = 100)
        private String phone;

        @Size(max = 500)
        private String note;

        @Size(max = 100)
        private String password;

        @Size(max = 1)
        private String deleteMark = "N";

        public static MemberForm empty() {
            return new MemberForm();
        }

        public static MemberForm from(Member member) {
            MemberForm form = new MemberForm();
            if (member.getId() != null) {
                form.setMemberId(member.getId().getMemberId());
            }
            form.setName(member.getName());
            form.setDeptId(member.getDeptId());
            form.setEmail(member.getEmail());
            form.setPhone(member.getPhone());
            form.setNote(member.getNote());
            form.setDeleteMark(member.getDeleteMark());
            return form;
        }

        public Member toEntity() {
            Member member = new Member();
            member.setId(new MemberId(null, memberId));
            member.setName(name);
            member.setDeptId(deptId);
            member.setEmail(email);
            member.setPhone(phone);
            member.setNote(note);
            member.setDeleteMark(deleteMark != null ? deleteMark : "N");
            return member;
        }

        public String getMemberId() {
            return memberId;
        }

        public void setMemberId(String memberId) {
            this.memberId = memberId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDeptId() {
            return deptId;
        }

        public void setDeptId(String deptId) {
            this.deptId = deptId;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getNote() {
            return note;
        }

        public void setNote(String note) {
            this.note = note;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getDeleteMark() {
            return deleteMark;
        }

        public void setDeleteMark(String deleteMark) {
            this.deleteMark = deleteMark;
        }
    }

    public static class MemberCreateRequest {
        public String member_id;
        public String name;
        public String dept_id;
        public String email;
        public String phone;
        public String note;
        public String password; // raw password, will be encoded
        public String delete_mark; // optional, default N
        public String actor; // optional auditing user id
    }
}
