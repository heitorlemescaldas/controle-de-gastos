import type { CategoryNode } from "@/services/categories";
export type CategoryTreeNode = CategoryNode & { children: CategoryTreeNode[] };

export function buildCategoryTree(nodes: CategoryNode[]): CategoryTreeNode[] {
  const byId = new Map<string, CategoryTreeNode>();
  const roots: CategoryTreeNode[] = [];
  nodes.forEach(n => byId.set(n.id, { ...n, children: [] }));
  for (const n of byId.values()) {
    if (n.parentId && byId.has(n.parentId)) byId.get(n.parentId)!.children.push(n);
    else roots.push(n);
  }
  const sortRec = (arr: CategoryTreeNode[]) => {
    arr.sort((a, b) => a.name.localeCompare(b.name));
    arr.forEach(c => sortRec(c.children));
  };
  sortRec(roots);
  return roots;
}