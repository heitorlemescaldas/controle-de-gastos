import type { CategoryNode } from "@/services/categories";

export type CategoryTreeNode = CategoryNode & { children: CategoryTreeNode[] };

export function buildCategoryTree(nodes: CategoryNode[]): CategoryTreeNode[] {
  const byId = new Map<string, CategoryTreeNode>();
  const roots: CategoryTreeNode[] = [];

  // clona + prepara children
  nodes.forEach(n => byId.set(n.id, { ...n, children: [] }));

  // liga pais e filhos
  for (const node of byId.values()) {
    if (node.parentId && byId.has(node.parentId)) {
      byId.get(node.parentId)!.children.push(node);
    } else {
      roots.push(node);
    }
  }

  // (opcional) ordena alfabeticamente
  const sortRec = (arr: CategoryTreeNode[]) => {
    arr.sort((a, b) => a.name.localeCompare(b.name));
    arr.forEach(n => sortRec(n.children));
  };
  sortRec(roots);

  return roots;
}
