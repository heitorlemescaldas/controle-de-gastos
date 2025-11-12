package br.ifsp.demo.controller;

import br.ifsp.demo.domain.model.Category;
import br.ifsp.demo.domain.model.CategoryNode;
import br.ifsp.demo.domain.port.CategoryRepositoryPort;
import br.ifsp.demo.domain.service.CategoryService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {

    private final CategoryService service;
    private final CategoryRepositoryPort repo;

    public CategoryController(CategoryService service, CategoryRepositoryPort repo){
        this.service = service;
        this.repo = repo;
    }

    @ApiResponse(responseCode = "201", description = "Categoria raiz criada")
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CategoryNode> createRoot(
        @Parameter(name = "X-User", description = "E-mail do usuário", in = ParameterIn.HEADER, required = true, example = "test@ex.com")
        @RequestHeader("X-User") String userId,
        @RequestBody CreateCategoryRequest req
    ){
        var saved = service.create(Category.root(userId, req.name()));
        var node = repo.findNodeById(saved.id(), userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(node);
    }

    @ApiResponse(responseCode = "201", description = "Subcategoria criada")
    @PostMapping(path = "/{parentId}/children", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CategoryNode> createChild(
        @Parameter(name = "X-User", description = "E-mail do usuário", in = ParameterIn.HEADER, required = true, example = "test@ex.com")
        @RequestHeader("X-User") String userId,
        @PathVariable String parentId,
        @RequestBody CreateCategoryRequest req
    ){
        var saved = service.create(Category.child(userId, req.name(), parentId));
        var node = repo.findNodeById(saved.id(), userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(node);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(
        @Parameter(name = "X-User", description = "E-mail do usuário", in = ParameterIn.HEADER, required = true, example = "test@ex.com")
        @RequestHeader("X-User") String userId,
        @PathVariable String id
    ){
        service.delete(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/rename")
    public ResponseEntity<?> rename(
        @Parameter(name = "X-User", description = "E-mail do usuário", in = ParameterIn.HEADER, required = true, example = "test@ex.com")
        @RequestHeader("X-User") String userId,
        @PathVariable String id,
        @RequestBody RenameRequest req
    ){
        service.rename(id, userId, req.newName());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/move")
    public ResponseEntity<?> move(
        @Parameter(name = "X-User", description = "E-mail do usuário", in = ParameterIn.HEADER, required = true, example = "test@ex.com")
        @RequestHeader("X-User") String userId,
        @PathVariable String id,
        @RequestBody MoveRequest req
    ){
        service.move(id, req.newParentId(), userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<CategoryNode>> list(
        @Parameter(name = "X-User", description = "E-mail do usuário", in = ParameterIn.HEADER, required = true, example = "test@ex.com")
        @RequestHeader("X-User") String userId
    ){
        return ResponseEntity.ok(service.listOrdered(userId));
    }

    // DTOs
    public record CreateCategoryRequest(String name){}
    public record RenameRequest(String newName){}
    public record MoveRequest(String newParentId){}
}