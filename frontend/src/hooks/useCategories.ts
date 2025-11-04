import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
  listCategories,
  createRootCategory,
  createChildCategory,
  renameCategory,
  moveCategory,
  deleteCategory,
  type CategoryNode,
  type CreateCategoryRequest,
  type RenameRequest,
  type MoveRequest,
} from "@/services/categories";

export function useCategories() {
  const qc = useQueryClient();

  const categoriesQuery = useQuery<CategoryNode[]>({
    queryKey: ["categories"],
    queryFn: () => listCategories(),
  });

  const invalidate = () => qc.invalidateQueries({ queryKey: ["categories"] });

  const createRoot = useMutation({
    mutationFn: (body: CreateCategoryRequest) => createRootCategory(body),
    onSuccess: invalidate,
  });

  const createChild = useMutation({
    mutationFn: ({ parentId, body }: { parentId: string; body: CreateCategoryRequest }) =>
      createChildCategory(parentId, body),
    onSuccess: invalidate,
  });

  const rename = useMutation({
    mutationFn: ({ id, body }: { id: string; body: RenameRequest }) => renameCategory(id, body),
    onSuccess: invalidate,
  });

  const move = useMutation({
    mutationFn: ({ id, body }: { id: string; body: MoveRequest }) => moveCategory(id, body),
    onSuccess: invalidate,
  });

  const removeCat = useMutation({
    mutationFn: (id: string) => deleteCategory(id),
    onSuccess: invalidate,
  });

  return {
    categoriesQuery,
    createRoot,
    createChild,
    rename,
    move,
    removeCat,
  };
}
