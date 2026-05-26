package com.student.studentmanagement.controller;

import com.student.studentmanagement.model.Student;
import com.student.studentmanagement.service.StudentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping
    public String listStudents(@RequestParam(required = false) String query, Model model) {
        List<Student> students;
        if (query != null && !query.isBlank()) {
            students = studentService.searchStudents(query);
            model.addAttribute("query", query);
        } else {
            students = studentService.getAllStudents();
            model.addAttribute("query", "");
        }
        model.addAttribute("students", students);
        model.addAttribute("totalCount", studentService.countStudents());
        return "students/list";
    }

    @GetMapping("/new")
    public String showAddForm(Model model) {
        model.addAttribute("student", new Student());
        return "students/form";
    }

    @PostMapping("/save")
    public String saveStudent(@ModelAttribute Student student, RedirectAttributes redirectAttrs) {
        boolean isNew = (student.getId() == null);

        if (student.getFirstName() == null || student.getFirstName().isBlank() ||
            student.getLastName() == null  || student.getLastName().isBlank() ||
            student.getEmail() == null     || student.getEmail().isBlank() ||
            student.getDepartment() == null || student.getDepartment().isBlank()) {
            redirectAttrs.addFlashAttribute("errorMessage", "All fields except marks are required.");
            if (isNew) return "redirect:/students/new";
            return "redirect:/students/edit/" + student.getId();
        }

        if (student.getMarks() != null && (student.getMarks() < 0 || student.getMarks() > 100)) {
            redirectAttrs.addFlashAttribute("errorMessage", "Marks must be between 0 and 100.");
            if (isNew) return "redirect:/students/new";
            return "redirect:/students/edit/" + student.getId();
        }

        studentService.saveStudent(student);

        String name = student.getFirstName() + " " + student.getLastName();
        redirectAttrs.addFlashAttribute("successMessage",
                isNew ? "Student \"" + name + "\" added successfully!"
                      : "Student \"" + name + "\" updated successfully!");
        return "redirect:/students";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("student", studentService.getStudentById(id));
        return "students/form";
    }

    @GetMapping("/delete/{id}")
    public String deleteStudent(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        Student student = studentService.getStudentById(id);
        String name = student.getFirstName() + " " + student.getLastName();
        studentService.deleteStudent(id);
        redirectAttrs.addFlashAttribute("successMessage",
                "Student \"" + name + "\" deleted successfully.");
        return "redirect:/students";
    }
}
