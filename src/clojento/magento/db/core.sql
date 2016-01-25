-- :name check :? :1
SELECT 'passed' as check

-- :name websites :? :*
SELECT website_id as id, code
FROM core_website

-- :name stores :? :*
SELECT store_id as id, code
FROM core_store
