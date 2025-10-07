package br.ifsp.demo.controller;

import br.ifsp.demo.domain.model.*;
import br.ifsp.demo.domain.service.CategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {

    private final CategoryService service;
    public CategoryController(CategoryService service){ this.service = service; }

    @PostMapping
    public ResponseEntity<?> createRoot(@RequestHeader("X-User") String userId, @RequestBody CreateCategoryRequest req){
        var c = service.create(Category.root(userId, req.name()));
        return ResponseEntity.ok(c);
    }

    @PostMapping("/{parentId}/children")
    public ResponseEntity<?> createChild(@RequestHeader("X-User") String userId, @PathVariable String parentId, @RequestBody CreateCategoryRequest req){
        var c = service.create(Category.child(userId, req.name(), parentId));
        return ResponseEntity.ok(c);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@RequestHeader("X-User") String userId, @PathVariable String id){
        service.delete(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/rename")
    public ResponseEntity<?> rename(@RequestHeader("X-User") String userId, @PathVariable String id, @RequestBody RenameRequest req){
        service.rename(id, userId, req.newName());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/move")
    public ResponseEntity<?> move(@RequestHeader("X-User") String userId, @PathVariable String id, @RequestBody MoveRequest req){
        service.move(id, req.newParentId(), userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<CategoryNode>> list(@RequestHeader("X-User") String userId){
        return ResponseEntity.ok(service.listOrdered(userId));
    }

    // DTOs
    public record CreateCategoryRequest(String name){}
    public record RenameRequest(String newName){}
    public record MoveRequest(String newParentId){}
}