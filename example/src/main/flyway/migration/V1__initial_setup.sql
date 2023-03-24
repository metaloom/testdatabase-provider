CREATE TYPE "embedding_types" AS ENUM (
  'DLIB_FACE_RESNET_v1'
);

/*
Enable UUID V4 Support
*/
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE "face" (
  "uuid" uuid DEFAULT uuid_generate_v4 (),
  "source" varchar UNIQUE NOT NULL,
  "face_nr" int NOT NULL,
  "blob" bytea NOT NULL,
  "meta" varchar,
  "type" embedding_types,
  PRIMARY KEY ("uuid")
);

COMMENT ON TABLE "face" IS 'Stores embeddings';

COMMENT ON COLUMN "face"."source" IS 'Identification of the source media';

COMMENT ON COLUMN "face"."face_nr" IS 'When the source contains more than one face the id can be used to identify them.';

COMMENT ON COLUMN "face"."blob" IS 'The actual embedding data';

COMMENT ON COLUMN "face"."meta" IS 'Custom meta properties to the face';
