ALTER TABLE public.resource
  ADD CONSTRAINT uq_resource_name_and_creator UNIQUE (name, created_by);

