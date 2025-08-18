-- Standardize transaction categories to match the new Category enum
-- This script updates existing transactions to use the standardized categories

-- Update Food & Dining related categories
UPDATE transactions 
SET category = 'Food & Dining' 
WHERE category IN ('food', 'dining', 'restaurant', 'groceries', 'lunch', 'dinner', 'breakfast', 'coffee', 'snacks')
   OR category ILIKE '%food%' 
   OR category ILIKE '%dining%' 
   OR category ILIKE '%restaurant%';

-- Update Shopping related categories
UPDATE transactions 
SET category = 'Shopping' 
WHERE category IN ('shopping', 'retail', 'clothing', 'electronics', 'amazon', 'target', 'walmart', 'online')
   OR category ILIKE '%shopping%' 
   OR category ILIKE '%retail%' 
   OR category ILIKE '%clothing%';

-- Update Transportation related categories
UPDATE transactions 
SET category = 'Transportation' 
WHERE category IN ('transport', 'gas', 'fuel', 'uber', 'lyft', 'taxi', 'bus', 'metro', 'parking')
   OR category ILIKE '%transport%' 
   OR category ILIKE '%gas%' 
   OR category ILIKE '%fuel%'
   OR category ILIKE '%uber%'
   OR category ILIKE '%lyft%';

-- Update Entertainment related categories
UPDATE transactions 
SET category = 'Entertainment' 
WHERE category IN ('entertainment', 'movies', 'games', 'hobbies', 'netflix', 'spotify', 'concert', 'theater')
   OR category ILIKE '%entertainment%' 
   OR category ILIKE '%movies%' 
   OR category ILIKE '%games%';

-- Update Utilities & Bills related categories
UPDATE transactions 
SET category = 'Utilities & Bills' 
WHERE category IN ('utilities', 'bills', 'electricity', 'water', 'internet', 'phone', 'cable', 'rent', 'mortgage')
   OR category ILIKE '%utilities%' 
   OR category ILIKE '%bills%' 
   OR category ILIKE '%electric%'
   OR category ILIKE '%internet%'
   OR category ILIKE '%phone%';

-- Update Healthcare related categories
UPDATE transactions 
SET category = 'Healthcare' 
WHERE category IN ('health', 'medical', 'pharmacy', 'doctor', 'hospital', 'dental', 'vision', 'insurance')
   OR category ILIKE '%health%' 
   OR category ILIKE '%medical%' 
   OR category ILIKE '%pharmacy%'
   OR category ILIKE '%doctor%';

-- Update Education related categories
UPDATE transactions 
SET category = 'Education' 
WHERE category IN ('education', 'school', 'training', 'courses', 'books', 'tuition', 'college', 'university')
   OR category ILIKE '%education%' 
   OR category ILIKE '%school%' 
   OR category ILIKE '%training%'
   OR category ILIKE '%courses%';

-- Update Travel related categories
UPDATE transactions 
SET category = 'Travel' 
WHERE category IN ('travel', 'vacation', 'hotel', 'airline', 'flight', 'trip', 'lodging', 'accommodation')
   OR category ILIKE '%travel%' 
   OR category ILIKE '%vacation%' 
   OR category ILIKE '%hotel%'
   OR category ILIKE '%airline%';

-- Set any remaining unrecognized categories to null
UPDATE transactions 
SET category = NULL 
WHERE category NOT IN (
    'Food & Dining',
    'Shopping', 
    'Transportation',
    'Entertainment',
    'Utilities & Bills',
    'Healthcare',
    'Education',
    'Travel'
);

-- Verify the results
SELECT category, COUNT(*) as count 
FROM transactions 
WHERE category IS NOT NULL 
GROUP BY category 
ORDER BY count DESC; 