CREATE TABLE "vector_store" (
  "id" uuid NOT NULL DEFAULT uuid_generate_v4(),
  "content" text COLLATE "pg_catalog"."default",
  "metadata" json,
  "embedding" "public"."vector",
  CONSTRAINT "vector_store_pkey" PRIMARY KEY ("id")
);