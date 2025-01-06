package io.github.smiley4.ktoropenapi.data

/**
 * Common configuration for tags.
 */
data class TagsData(
    val tags: List<TagData>,
    val generator: TagGenerator,
) {

    companion object {
        val DEFAULT = TagsData(
            tags = emptyList(),
            generator = { emptyList() }
        )
    }

}
