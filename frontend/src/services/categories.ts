import { api } from "@/lib/api";

/** Tipos conforme Swagger */
export type CategoryNode = {
  id: string;
  userId: string;
  name: string;
  parentId: string | null;
  path: string;
};

export type CreateCategoryRequest = {
  name: string;
};

export type RenameRequest = {
  newName: string;
};

export type MoveRequest = {
  newParentId: string;
};

/** GET /categories */
export async function listCategories(): Promise<CategoryNode[]> {
  return api.get<CategoryNode[]>("/categories");
}

/** POST /categories  (cria raiz) */
export async function createRootCategory(body: CreateCategoryRequest): Promise<void> {
  await api.post<void>("/categories", body);
}

/** POST /categories/{parentId}/children  (cria filha) */
export async function createChildCategory(parentId: string, body: CreateCategoryRequest): Promise<void> {
  await api.post<void>(`/categories/${parentId}/children`, body);
}

/** PATCH /categories/{id}/rename */
export async function renameCategory(id: string, body: RenameRequest): Promise<void> {
  await api.patch<void>(`/categories/${id}/rename`, body);
}

/** PATCH /categories/{id}/move */
export async function moveCategory(id: string, body: MoveRequest): Promise<void> {
  await api.patch<void>(`/categories/${id}/move`, body);
}

/** DELETE /categories/{id} */
export async function deleteCategory(id: string): Promise<void> {
  await api.del<void>(`/categories/${id}`);
}