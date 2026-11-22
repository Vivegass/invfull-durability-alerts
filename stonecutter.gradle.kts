stonecutter active "26.3"

// See https://stonecutter.kikugie.dev/wiki/config/params
stonecutter parameters {
    replacements {
        string(current.parsed >= "1.21.11") {
            replace("ResourceLocation", "Identifier")
        }
    }
}
