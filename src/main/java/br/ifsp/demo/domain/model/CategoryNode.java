package br.ifsp.demo.domain.model;

public record CategoryNode(
        String id,
        String userId,
        String name,
        String parentId,
        String path
) {}