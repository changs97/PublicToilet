package com.changs.publictoilet.data

data class SearchPublicToiletPOIService(
    val RESULT: RESULT,
    val list_total_count: Int,
    val row: List<Row>
)