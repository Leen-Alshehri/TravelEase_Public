package com.example.travelease.accommodationsApi

data class AccommodationResponse(
    val brands: List<Brand>?,
    val properties: List<Property>?
)

data class Brand(
    val id: Int,
    val name: String,
    val children: List<BrandChild>?
)

data class BrandChild(
    val id: Int,
    val name: String
)

data class Property(
    val type: String,
    val name: String,
    val description: String,
    val link: String,
    val logo: String,
    val sponsored: Boolean,
    val eco_certified: Boolean,
    val gps_coordinates: GpsCoordinates,
    val check_in_time: String,
    val check_out_time: String,
    val rate_per_night: Rate?,
    val total_rate: TotalRate?,
    val prices: List<PriceSource>?,
    val nearby_places: List<NearbyPlace>?,
    val hotel_class: String?,
    val extracted_hotel_class: Int?,
    val images: List<Image>?,
    val overall_rating: Float,
    val reviews: Int?,
    val ratings: List<Rating>?,
    val location_rating: Float?,
    val reviews_breakdown: List<ReviewBreakdown>?,
    val amenities: List<String>?,
    val excluded_amenities: List<String>?,
    val essential_info: List<String>?,
    val property_token: String,
    val serpapi_property_details_link: String,
    val address: String = ""
)

data class GpsCoordinates(
    val latitude: Float,
    val longitude: Float
)

data class Rate(
    val lowest: String,
    val extracted_lowest: Float,
    val before_taxes_fees: String,
    val extracted_before_taxes_fees: Float
)

data class TotalRate(
    val lowest: String,
    val extracted_lowest: Float,
    val before_taxes_fees: String,
    val extracted_before_taxes_fees: Float
)

data class PriceSource(
    val source: String,
    val logo: String,
    val rate_per_night: Rate
)

data class NearbyPlace(
    val name: String,
    val transportations: List<Transportation>
)

data class Transportation(
    val type: String,
    val duration: String
)

data class Image(
    val thumbnail: String,
    val original_image: String
)

data class Rating(
    val stars: Int,
    val count: Int
)

data class ReviewBreakdown(
    val name: String,
    val description: String,
    val total_mentioned: Int,
    val positive: Int,
    val negative: Int,
    val neutral: Int
)