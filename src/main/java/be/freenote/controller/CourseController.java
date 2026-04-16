package be.freenote.controller;

import be.freenote.dto.request.CreateCourseRequest;
import be.freenote.dto.response.CourseResponse;
import be.freenote.service.CourseService;
import be.freenote.security.ratelimit.RateLimit;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
@Tag(name = "Courses", description = "Course management")
public class CourseController {

    private final CourseService courseService;

    @GetMapping
    @Operation(summary = "Get courses by section",
               description = "Returns all approved courses for a given section. Public endpoint.")
    public ResponseEntity<List<CourseResponse>> getBySectionId(@RequestParam Long sectionId) {
        return ResponseEntity.ok(courseService.getBySectionId(sectionId));
    }

    @PostMapping
    @RateLimit(max = 5, window = 3600)
    @Operation(summary = "Create a course",
               description = "Creates a new course pending admin approval. Requires authentication.")
    public ResponseEntity<CourseResponse> create(Authentication authentication,
                                                  @Valid @RequestBody CreateCourseRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(courseService.create(request, userId));
    }
}
