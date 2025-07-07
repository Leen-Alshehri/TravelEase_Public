package com.example.travelease.firebaseDB

import android.util.Log
import com.example.travelease.firebaseDB.entities.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class dbRepository {
    private val db = FirebaseFirestore.getInstance()

    // TRAVELER FUNCTIONS
    suspend fun addTraveler(traveler: Traveler) {
        db.collection("Travelers").document(traveler.travelerId).set(traveler).await()
    }

    suspend fun getTravelerByEmail(email: String): Traveler? {
        val result = db.collection("Travelers").whereEqualTo("email", email).get().await()
        return result.documents.firstOrNull()?.toObject(Traveler::class.java)
    }

    suspend fun getTravelerById(travelerId: String): Traveler? {
        return try {
            val result = db.collection("Travelers").document(travelerId).get().await()
            result.toObject(Traveler::class.java) // Converts Firestore data to Traveler object
        } catch (e: Exception) {
            Log.e("Firestore", "Error fetching traveler: ${e.message}")
            null
        }
    }


    suspend fun updateTraveler(traveler: Traveler) {
        db.collection("Travelers").document(traveler.travelerId)
            .set(traveler)  // updates the Firestore document
            .await()
    }


    suspend fun deleteTraveler(travelerId: String) {
        db.collection("Travelers").document(travelerId).delete().await()
    }

    // PROFILE FUNCTIONS ---------------------------------------------------------
    suspend fun addProfile(profile: Profile) {
        db.collection("Profiles").document(profile.profileId).set(profile).await()
    }

    suspend fun getProfileByTraveler(travelerId: String): Profile? {
        val result = db.collection("Profiles").whereEqualTo("travelerId", travelerId).get().await()
        return result.documents.firstOrNull()?.toObject(Profile::class.java)
    }

    suspend fun deleteProfile(profileId: String) {
        db.collection("Profiles").document(profileId).delete().await()
    }

    suspend fun getHistory(travelerId: String): List<String> {
        val historyMap = mutableMapOf<String, String>()
        val allStrings = mutableListOf<String>()

        return try {
            val tripsSnapshot =
                db.collection("Travelers").document(travelerId).collection("Trips").get().await()

            for (trip in tripsSnapshot.documents) {
                val tripId = trip.id
                val itineraryId = trip.getString("itineraryId") ?: continue

                val accommodationsSnapshot = db.collection("Travelers").document(travelerId)
                    .collection("Trips").document(tripId)
                    .collection("Itinerary").document(itineraryId)
                    .collection("Accommodations").get().await()

                for (doc in accommodationsSnapshot.documents) {
                    val name = doc.getString("name") ?: ""
                    val desc = doc.getString("description") ?: ""
                    val rating = doc.getDouble("rating")?.toString() ?: ""
                    val reviews = doc.getLong("reviews")?.toString() ?: ""
                    val price = doc.getString("pricePerNight") ?: ""
                    val hotelClass = doc.getString("hotelClass") ?: ""
                    val combined = "$name $desc $rating $reviews $price $hotelClass"
                    allStrings.add(combined)
                }

                val activitiesSnapshot = db.collection("Travelers").document(travelerId)
                    .collection("Trips").document(tripId)
                    .collection("Itinerary").document(itineraryId)
                    .collection("Activities").get().await()

                for (doc in activitiesSnapshot.documents) {
                    val name = doc.getString("name") ?: ""
                    val desc = doc.getString("description") ?: ""
                    val rating = doc.getDouble("rating")?.toString() ?: ""
                    val reviews = doc.getLong("reviews")?.toString() ?: ""
                    val combined = "$name $desc $rating $reviews"
                    allStrings.add(combined)
                }
            }

            allStrings

        } catch (e: Exception) {
            Log.e("HistoryFetch", "Error: ${e.message}")
            emptyList() // fallback
        }
    }
    // getPreferences
    suspend fun getPreferences(travelerId: String): List<String>{
        return try {
            var preferencesList = mutableListOf<String>()
            val profileRef = db.collection("Travelers")
                .document(travelerId)
                .collection("Profile")
                .document("Main").get().await()
            val preferencesMap = profileRef.data as? Map<*, *>
            if (preferencesMap != null) {
                for (i in preferencesMap.keys){
                    preferencesMap[i]?.let { preferencesList.add(it.toString()) }
                }
            }
            preferencesList
        }catch (e: Exception) {
            Log.e("Firestore", "Error fetching traveler preferences: ${e.message}")
            emptyList()
        }
    }


    // TRIP FUNCTIONS ---------------------------------------------------------------
    suspend fun getTripsByName(tripName: String, travelerId: String): Trip? {
        return try {
            val result = db.collection("Travelers").document(travelerId)
                .collection("Trips").whereEqualTo("name", tripName).get().await()
            result.toObjects(Trip::class.java).firstOrNull()
        } catch (e: Exception) {
            println("fetching trip by name Error: ${e.message}")
            null
        }
    }

    suspend fun checkTripExistsByName(tripName: String, travelerId: String): Boolean {
        return try {
            val result = db.collection("Travelers").document(travelerId)
                .collection("Trips").whereEqualTo("name", tripName).get().await()
            val trip = result.toObjects(Trip::class.java)
            trip.isNotEmpty()
        } catch (e: Exception) {
            println("checking trip existence by name Error: ${e.message}")
            false
        }

    }

    suspend fun addTripInRepository(
        travelerId: String, name: String, startDate: String,
        endDate: String, imageUrl: String? = null,
        onSuccess: () -> Unit
    ) {
        val tripId = UUID.randomUUID().toString()
        val itineraryId = UUID.randomUUID().toString()

        val trip = Trip(
            tripId = tripId,
            travelerId = travelerId,
            itineraryId = itineraryId,
            name = name,
            startDate = startDate,
            endDate = endDate,
            imageUrl = imageUrl
        )
        val tripRef = db.collection("Travelers").document(travelerId)
            .collection("Trips").document(tripId)

        try {
            tripRef.set(trip).await()
            Log.d("Firestore", "Trip added successfully: $tripId")

            // Automatically create an itinerary inside this trip
            val itinerary = Itinerary(
                itineraryId = itineraryId,
                tripId = tripId,
                date = SimpleDateFormat(
                    "yyyy-MM-dd",
                    Locale.getDefault()
                ).format(Date()) // Current date
            )

            val itineraryRef = tripRef.collection("Itinerary").document(itineraryId)
            itineraryRef.set(itinerary).await()
            Log.d("Firestore", "Itinerary created successfully: $itineraryId")

            val docRef = db
                .collection("Travelers").document(travelerId)
                .collection("Trips").document(tripId)
                .collection("Itinerary").document(itineraryId)
                .collection("Budget").document("Main")

            val budget = Budget(
                budgetId = "Main",
                tripId = tripId,
                itineraryId = itineraryId,
                budget = 0.00f.toDouble(),
                netBalance = 0.00f.toDouble(),
                totalExpenses = 0.0
            )

            docRef.set(budget)
                .addOnSuccessListener {
                    onSuccess()
                }
                .addOnFailureListener {
                    it.printStackTrace()
                }
        } catch (e: Exception) {
            Log.e("Firestore", "Error adding trip and itinerary: ${e.message}")
        }
    }

    suspend fun addFlightToItineraryInRepository(
        travelerId: String?,
        tripId: String?,
        itineraryId: String?,
        flight: Flight, // Using `Flight` entity
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        if (travelerId.isNullOrEmpty() || tripId.isNullOrEmpty() || itineraryId.isNullOrEmpty()) {
            Log.e(
                "Firestore",
                "Invalid IDs - Traveler ID: $travelerId, Trip ID: $tripId, Itinerary ID: $itineraryId"
            )
            onFailure(IllegalArgumentException("Invalid Firestore document reference: IDs cannot be empty"))
            return
        }

        val db = FirebaseFirestore.getInstance()
        val flightsCollection = db.collection("Travelers").document(travelerId)
            .collection("Trips").document(tripId)
            .collection("Itinerary").document(itineraryId)
            .collection("Flights")


        try {
            flightsCollection.document(flight.flightId).set(flight).await()
            Log.d("Firestore", "Flight added successfully!")
            onSuccess()
        } catch (e: Exception) {
            Log.e("Firestore", "Error adding flight: ${e.message}")
            onFailure(e)
        }
    }

    suspend fun addAccommodationToItineraryInRepository(
        travelerId: String?,
        tripId: String?,
        itineraryId: String?,
        accommodation: Accommodation,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        if (travelerId.isNullOrEmpty() || tripId.isNullOrEmpty() || itineraryId.isNullOrEmpty()) {
            Log.e(
                "Firestore",
                "Invalid IDs - Traveler ID: $travelerId, Trip ID: $tripId, Itinerary ID: $itineraryId"
            )
            onFailure(IllegalArgumentException("Invalid Firestore document reference: IDs cannot be empty"))
            return
        }

        val db = FirebaseFirestore.getInstance()
        val accommodationsCollection = db.collection("Travelers").document(travelerId)
            .collection("Trips").document(tripId)
            .collection("Itinerary").document(itineraryId)
            .collection("Accommodations")

        try {
            accommodationsCollection.document(accommodation.accommodationId).set(accommodation)
                .await()
            Log.d("Firestore", "Accommodation added successfully!")
            onSuccess()
        } catch (e: Exception) {
            Log.e("Firestore", "Error adding accommodation: ${e.message}")
            onFailure(e)
        }
    }

    suspend fun addActivityToItineraryInRepository(
        travelerId: String?,
        tripId: String?,
        itineraryId: String?,
        activity: Activity,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {

        if (travelerId.isNullOrEmpty() || tripId.isNullOrEmpty() || itineraryId.isNullOrEmpty()) {
            Log.e(
                "Firestore",
                "Invalid IDs - Traveler ID: $travelerId, Trip ID: $tripId, Itinerary ID: $itineraryId"
            )
            onFailure(IllegalArgumentException("Invalid Firestore document reference: IDs cannot be empty"))
            return
        }

        val db = FirebaseFirestore.getInstance()
        val activitiesCollection = db.collection("Travelers").document(travelerId)
            .collection("Trips").document(tripId)
            .collection("Itinerary").document(itineraryId)
            .collection("Activities")

        try {
            activitiesCollection.document(activity.activityId).set(activity).await()
            Log.d("Firestore", "Activity added successfully!")
            onSuccess()
        } catch (e: Exception) {
            Log.e("Firestore", "Error adding activity: ${e.message}")
            onFailure(e)
        }

    }

    suspend fun getItinerariesByTrip(tripId: String): List<Itinerary> {
        val result = db.collection("Itineraries").whereEqualTo("tripId", tripId).get().await()
        return result.toObjects(Itinerary::class.java)
    }

}

