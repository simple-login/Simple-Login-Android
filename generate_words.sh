#!/bin/bash

# Configuration
URL="https://raw.githubusercontent.com/simple-login/app/master/local_data/words.txt"
OUTPUT_FILE="app/src/main/java/io/simplelogin/android/data/models/preferences/WordList.kt"
PACKAGE_NAME="io.simplelogin.android.data.models.preferences"

# 1. Ensure the directory exists before writing
mkdir -p "$(dirname "$OUTPUT_FILE")"

echo "Fetching words from $URL"

# 2. Fetch and clean data
# We filter out empty lines to ensure the Kotlin list is valid
RAW_DATA=$(curl -s "$URL" | grep -v '^$')
WORD_COUNT=$(echo "$RAW_DATA" | wc -l | xargs)

echo "Generating $OUTPUT_FILE with $WORD_COUNT words..."

# 3. Write the file header
cat <<EOF > "$OUTPUT_FILE"
package $PACKAGE_NAME

/**
 * Automatically generated word list.
 * Do not modify but run generate_words.sh instead
 */
object WordList {
    val words: List<String> = listOf(
EOF

# 4. Format words and append
# We use 'printf' style formatting via awk for cleaner output
echo "$RAW_DATA" | awk '{ printf "        \"%s\",\n", $1 }' >> "$OUTPUT_FILE"

# 5. Close the object
cat <<EOF >> "$OUTPUT_FILE"
    )
}
EOF

echo "Success! Generated $WORD_COUNT words in $OUTPUT_FILE."