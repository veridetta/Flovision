package com.vr.flovision.model

data class SpeciesQuery(
    val query: Query,
    val language: String,
    val preferedReferential: String,
    val bestMatch: String,
    val results: List<Result>,
    val version: String,
    val remainingIdentificationRequests: Int
)

data class Query(
    val project: String,
    val images: List<String>,
    val organs: List<String>,
    val includeRelatedImages: Boolean
)

data class Result(
    val score: Double,
    val species: Species,
    val gbif: Gbif,
    val powo: Powo
)

data class Species(
    val scientificNameWithoutAuthor: String,
    val scientificNameAuthorship: String,
    val genus: Genus,
    val family: Family,
    val commonNames: List<String>,
    val scientificName: String
)

data class Genus(
    val scientificNameWithoutAuthor: String,
    val scientificNameAuthorship: String,
    val scientificName: String
)

data class Family(
    val scientificNameWithoutAuthor: String,
    val scientificNameAuthorship: String,
    val scientificName: String
)

data class Gbif(
    val id: String
)

data class Powo(
    val id: String
)


