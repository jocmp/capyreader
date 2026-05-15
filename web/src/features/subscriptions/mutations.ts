import { useMutation, useQueryClient } from "@tanstack/react-query";
import { useAuth } from "@/auth/AuthContext";
import { minifluxApi } from "@/api/miniflux";

function useCredentials() {
  const { credentials } = useAuth();
  if (!credentials) throw new Error("Mutation requires signed-in credentials");
  return credentials;
}

function invalidateSubscriptionQueries(
  queryClient: ReturnType<typeof useQueryClient>,
) {
  queryClient.invalidateQueries({ queryKey: ["feeds"] });
  queryClient.invalidateQueries({ queryKey: ["categories"] });
  queryClient.invalidateQueries({ queryKey: ["counters"] });
  queryClient.invalidateQueries({ queryKey: ["entries"] });
}

export function useCreateFeed() {
  const credentials = useCredentials();
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (params: { feed_url: string; category_id: number }) =>
      minifluxApi.createFeed(credentials, params),
    onSuccess: () => invalidateSubscriptionQueries(queryClient),
  });
}

export function useDeleteFeed() {
  const credentials = useCredentials();
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (feedId: number) => minifluxApi.deleteFeed(credentials, feedId),
    onSuccess: () => invalidateSubscriptionQueries(queryClient),
  });
}

export function useUpdateFeed() {
  const credentials = useCredentials();
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (params: {
      feedId: number;
      category_id?: number;
      title?: string;
    }) =>
      minifluxApi.updateFeed(credentials, params.feedId, {
        category_id: params.category_id,
        title: params.title,
      }),
    onSuccess: () => invalidateSubscriptionQueries(queryClient),
  });
}

export function useDiscoverFeeds() {
  const credentials = useCredentials();
  return useMutation({
    mutationFn: (url: string) => minifluxApi.discoverFeeds(credentials, url),
  });
}

export function useCreateCategory() {
  const credentials = useCredentials();
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (title: string) =>
      minifluxApi.createCategory(credentials, title),
    onSuccess: () => invalidateSubscriptionQueries(queryClient),
  });
}

export function useDeleteCategory() {
  const credentials = useCredentials();
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (categoryId: number) =>
      minifluxApi.deleteCategory(credentials, categoryId),
    onSuccess: () => invalidateSubscriptionQueries(queryClient),
  });
}

export function useImportOpml() {
  const credentials = useCredentials();
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (xml: string) => minifluxApi.importOpml(credentials, xml),
    onSuccess: () => invalidateSubscriptionQueries(queryClient),
  });
}

export function useExportOpml() {
  const credentials = useCredentials();
  return useMutation({
    mutationFn: () => minifluxApi.exportOpml(credentials),
  });
}
