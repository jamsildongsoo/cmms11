package com.cmms11.web;

import com.cmms11.domain.member.Member;
import com.cmms11.domain.member.MemberId;
import com.cmms11.domain.member.MemberService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/members")
public class MemberController {
    private final MemberService service;

    public MemberController(MemberService service) {
        this.service = service;
    }

    @GetMapping
    public Page<Member> list(@RequestParam(name = "q", required = false) String q, Pageable pageable) {
        return service.list(q, pageable);
    }

    @GetMapping("/{memberId}")
    public ResponseEntity<Member> get(@PathVariable String memberId) {
        return ResponseEntity.ok(service.get(memberId));
    }

    @PostMapping
    public ResponseEntity<Member> create(@Valid @RequestBody MemberCreateRequest req) {
        Member member = new Member();
        member.setId(new MemberId(null, req.member_id));
        member.setName(req.name);
        member.setDeptId(req.dept_id);
        member.setEmail(req.email);
        member.setPhone(req.phone);
        member.setNote(req.note);
        if (req.delete_mark != null) member.setDeleteMark(req.delete_mark);
        Member saved = service.create(member, req.password, req.actor);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
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
