package com.cmms11.web;

import com.cmms11.domain.member.Member;
import com.cmms11.domain.member.MemberId;
import com.cmms11.domain.member.MemberService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 사용자(회원) 기준정보 화면 컨트롤러.
 */
@Controller
@RequestMapping("/domain/member")
@Validated
public class MemberViewController {

    private final MemberService service;

    public MemberViewController(MemberService service) {
        this.service = service;
    }

    @GetMapping("/list")
    public String list(
        @RequestParam(name = "q", required = false) String keyword,
        Pageable pageable,
        Model model
    ) {
        Page<Member> page = service.list(keyword, pageable);
        model.addAttribute("page", page);
        model.addAttribute("keyword", keyword);
        return "domain/member/list";
    }

    @GetMapping("/form")
    public String newForm(Model model) {
        model.addAttribute("member", MemberForm.empty());
        model.addAttribute("isNew", true);
        return "domain/member/form";
    }

    @GetMapping("/edit/{memberId}")
    public String editForm(@PathVariable String memberId, Model model) {
        Member member = service.get(memberId);
        model.addAttribute("member", MemberForm.from(member));
        model.addAttribute("isNew", false);
        return "domain/member/form";
    }

    @PostMapping("/save")
    public String save(
        @ModelAttribute @Validated MemberForm form,
        @RequestParam(name = "isNew", required = false) String isNew
    ) {
        if ("true".equals(isNew)) {
            service.create(form.toEntity(), form.getPassword(), null);
        } else {
            service.update(form.toEntity(), form.getPassword(), null);
        }
        return "redirect:/domain/member/list";
    }

    @PostMapping("/delete/{memberId}")
    public String delete(@PathVariable String memberId) {
        service.delete(memberId);
        return "redirect:/domain/member/list";
    }

    /**
     * 화면과 서비스 계층 사이의 폼 데이터 매핑용 클래스.
     */
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

        @Size(max = 1)
        private String deleteMark = "N";

        @Size(max = 100)
        private String password;

        public static MemberForm empty() {
            return new MemberForm();
        }

        public static MemberForm from(Member member) {
            MemberForm form = new MemberForm();
            form.setMemberId(member.getId().getMemberId());
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

        public String getDeleteMark() {
            return deleteMark;
        }

        public void setDeleteMark(String deleteMark) {
            this.deleteMark = deleteMark;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}

