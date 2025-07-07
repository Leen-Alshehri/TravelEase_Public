package com.example.travelease.firebaseDB
import androidx.lifecycle.*
import com.example.travelease.firebaseDB.entities.*
import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import android.util.Log
import com.example.travelease.recommenderSystem.activity
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.*
import java.util.UUID
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.Date


class dbViewModel (private val repository: dbRepository) : ViewModel(){
    private val db = FirebaseFirestore.getInstance()
    private val _postsLiveData = MutableLiveData<List<Post>>()
    val postsLiveData: LiveData<List<Post>> get() = _postsLiveData

    //--------------------------------------------------------------------
        // TRAVELER FUNCTIONS

        fun getTraveler(travelerId: String, onResult: (Traveler?) -> Unit) {
            viewModelScope.launch {
                onResult(repository.getTravelerById(travelerId))
            }
        }

        fun updateTraveler(traveler: Traveler) {
                viewModelScope.launch {
                    repository.updateTraveler(traveler) // Call repository function
                }
            }

        // TRIP FUNCTIONS ----------------------------------------------------------------

        private val _currentTrips = MutableStateFlow<List<Trip>>(emptyList())
        val currentTrips: StateFlow<List<Trip>> = _currentTrips

        private val _upcomingTrips = MutableStateFlow<List<Trip>>(emptyList())
        val upcomingTrips: StateFlow<List<Trip>> = _upcomingTrips

        private val _previousTrips = MutableStateFlow<List<Trip>>(emptyList())
        val previousTrips: StateFlow<List<Trip>> = _previousTrips

        private val _tripRetrievedByName = MutableStateFlow<Trip?>(null)
        val tripRetrievedByName: StateFlow<Trip?> = _tripRetrievedByName

    fun fetchTrips(travelerId: String) { //get all traveler's trips
        viewModelScope.launch {
            try {
                val result = db.collection("Travelers").document(travelerId)
                    .collection("Trips").get().await()

                val allTrips = result.documents.mapNotNull { doc ->
                    val trip = doc.toObject(Trip::class.java)

                    // Ensure itineraryId is included
                    if (trip != null) {
                        trip.copy(itineraryId = doc.getString("itineraryId") ?: "")
                    } else null
                }
                categorizeTrips(allTrips)
            } catch (e: Exception) {
                Log.e("Firestore", "Error fetching trips: ${e.message}")
            }
        }
    }


    // Categorizing trips (Current, Upcoming, Previous)
        private fun categorizeTrips(trips: List<Trip>) {
            val today = Calendar.getInstance().time // Get current date
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

            _currentTrips.value = trips.filter { trip ->
                try {
                    val start = dateFormat.parse(trip.startDate)
                    val end = dateFormat.parse(trip.endDate)
                    start != null && end != null && !start.after(today) && !end.before(today)
                } catch (e: Exception) {
                    false
                }
            }

            _upcomingTrips.value = trips.filter { trip ->
                try {
                    val start = dateFormat.parse(trip.startDate)
                    start != null && start.after(today)
                } catch (e: Exception) {
                    false
                }
            }

            _previousTrips.value = trips.filter { trip ->
                try {
                    val end = dateFormat.parse(trip.endDate)
                    end != null && end.before(today)
                } catch (e: Exception) {
                    false
                }
            }
        }

    fun isPreviousTripById(tripId: String): Boolean {
        return _previousTrips.value.any { it.tripId == tripId }
    }



fun addTrip(travelerId: String, name: String, startDate: String, endDate: String, imageUrl: String? = null) {
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

    viewModelScope.launch {
        val tripRef = db.collection("Travelers").document(travelerId)
            .collection("Trips").document(tripId)

        try {
            tripRef.set(trip).await()
            Log.d("Firestore", "Trip added successfully: $tripId")

            // Automatically create an itinerary inside this trip
            val itinerary = Itinerary(
                itineraryId = itineraryId,
                tripId = tripId,
                date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) // Current date
            )

            val itineraryRef = tripRef.collection("Itinerary").document(itineraryId)
            itineraryRef.set(itinerary).await()
            Log.d("Firestore", "Itinerary created successfully: $itineraryId")

            fetchTrips(travelerId) //this refresh the UI
            Log.d("Firestore", "Subcollections created successfully for itinerary: $itineraryId")
            addBudget(travelerId,tripId,itineraryId,0.00f){}
        } catch (e: Exception) {
            Log.e("Firestore", "Error adding trip and itinerary: ${e.message}")
        }
    }
}
    // only used if the trip exists
fun getTripByNameForFlights(tripName: String,
                            travelerId: String,
                            startDate: String,
                            endDate: String,
                            imageUri: String?, flight: Flight, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val exists = repository.checkTripExistsByName(tripName, travelerId)
                if (!exists) {
                    repository.addTripInRepository(travelerId, tripName, startDate, endDate,
                        imageUri, onSuccess)
                }
                _tripRetrievedByName.value = repository.getTripsByName(tripName, travelerId)
                repository.addFlightToItineraryInRepository(travelerId,
                    _tripRetrievedByName.value?.tripId, _tripRetrievedByName.value?.itineraryId,
                    flight,{},{})
            }catch (e: Exception) {
                Log.e("TripFetch", "Error: ${e.message}")
                onSuccess() // fallback
            }
        }
}

    fun getTripByNameForAccommodations(tripName: String,
                                   travelerId: String,
                                   startDate: String,
                                   endDate: String,
                                   imageUri: String?, accommodation: Accommodation,
                                   onSuccess: () -> Unit){
        viewModelScope.launch {
            try {
                val exists = repository.checkTripExistsByName(tripName, travelerId)
                if (!exists) {
                    repository.addTripInRepository(travelerId, tripName, startDate, endDate,
                        imageUri, onSuccess)
                }
                _tripRetrievedByName.value = repository.getTripsByName(tripName, travelerId)
                repository.addAccommodationToItineraryInRepository(travelerId,
                    _tripRetrievedByName.value?.tripId, _tripRetrievedByName.value?.itineraryId,
                    accommodation,{},{})
            }catch (e: Exception) {
                Log.e("TripFetch", "Error: ${e.message}")
                onSuccess() // fallback
            }
        }
}

    fun getTripByNameForActivities(tripName: String,
                                   travelerId: String,
                                   startDate: String,
                                   endDate: String,
                                   imageUri: String?, activity: Activity,
                                   onSuccess: () -> Unit){
        viewModelScope.launch {
            try {
                val exists = repository.checkTripExistsByName(tripName, travelerId)
                if (!exists) {
                    repository.addTripInRepository(travelerId, tripName, startDate, endDate,
                        imageUri, onSuccess)
                }
                _tripRetrievedByName.value = repository.getTripsByName(tripName, travelerId)
                repository.addActivityToItineraryInRepository(travelerId,
                    _tripRetrievedByName.value?.tripId, _tripRetrievedByName.value?.itineraryId,
                    activity,{},{})
                Log.d("FireStore","activity added successfully")
            }catch (e: Exception) {
                Log.e("TripFetch", "Error: ${e.message}")
                onSuccess() // fallback
            }
        }

    }

    fun getTripBuNameForAddAll(tripName: String,
                               travelerId: String,
                               startDate: String,
                               endDate: String,
                               imageUri: String?, activities: List<activity>,
                               accommodation: Accommodation,
                               flight: Flight,
                               onSuccess: () -> Unit){
        viewModelScope.launch {
            try {
                val exists = repository.checkTripExistsByName(tripName, travelerId)
                if (!exists) {
                    repository.addTripInRepository(
                        travelerId, tripName, startDate, endDate,
                        imageUri, onSuccess
                    )
                }
                _tripRetrievedByName.value = repository.getTripsByName(tripName, travelerId)
                // ---- Flights ---- //
                if (flight.flightId != "")
                    repository.addFlightToItineraryInRepository(travelerId,
                        _tripRetrievedByName.value?.tripId, _tripRetrievedByName.value?.itineraryId,
                        flight, {}, {})
                // ----- Accommodations ----- //
                if (accommodation.name != "")
                    repository.addAccommodationToItineraryInRepository(travelerId,
                        _tripRetrievedByName.value?.tripId, _tripRetrievedByName.value?.itineraryId,
                        accommodation, {}, {})
                // ----- Activities ----- //
                if (activities.isNotEmpty()) {
                    for (act in activities) {
                        val activityData = Activity(
                            name = act.title,
                            itineraryId = "",
                            sdate = act.date,
                            location = act.address.joinToString(", "),
                            // TODO: need to send link and check the date
                            link = "",
                            activityId = UUID.randomUUID().toString(),
                            rating = act.rating,
                            description = act.description ?: "",
                            reviews = act.reviews
                        )
                        repository.addActivityToItineraryInRepository(travelerId,
                            _tripRetrievedByName.value?.tripId,
                            _tripRetrievedByName.value?.itineraryId,
                            activityData,
                            {},
                            {})
                    }
                }
            }catch (e: Exception) {
                Log.e("TripFetch for add all button", "Error: ${e.message}")
                onSuccess() // fallback
            }
        }

    }


    fun deleteTrip(tripId: String, travelerId: String) {
        viewModelScope.launch {
            db.collection("Travelers").document(travelerId)
                .collection("Trips").document(tripId)
                .delete()
                .addOnSuccessListener {
                    fetchTrips(travelerId) // Refresh UI after delete
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore", "Error deleting trip: ${e.message}")
                }
        }
    }



    fun addFlightToItinerary(
        travelerId: String?,
        tripId: String?,
        itineraryId: String?,
        flight: Flight,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        if (travelerId.isNullOrEmpty() || tripId.isNullOrEmpty() || itineraryId.isNullOrEmpty()) {
            Log.e("Firestore", "Invalid IDs - Traveler ID: $travelerId, Trip ID: $tripId, Itinerary ID: $itineraryId")
            onFailure(IllegalArgumentException("Invalid Firestore document reference: IDs cannot be empty"))
            return
        }

        val db = FirebaseFirestore.getInstance()
        val flightsCollection = db.collection("Travelers").document(travelerId)
            .collection("Trips").document(tripId)
            .collection("Itinerary").document(itineraryId)
            .collection("Flights")

        viewModelScope.launch {
            try {
                flightsCollection.document(flight.flightId).set(flight).await()
                Log.d("Firestore", "Flight added successfully!")
                onSuccess()
            } catch (e: Exception) {
                Log.e("Firestore", "Error adding flight: ${e.message}")
                onFailure(e)
            }
        }
    }

    fun getFlightsByItinerary(travelerId: String, tripId: String, itineraryId: String, onResult: (List<Flight>) -> Unit) {
        val flightsCollection = FirebaseFirestore.getInstance()
            .collection("Travelers").document(travelerId)
            .collection("Trips").document(tripId)
            .collection("Itinerary").document(itineraryId)
            .collection("Flights")

        if (itineraryId.isBlank()) {
            Log.e("Firestore", "itineraryId is empty — in getFlightsByItinerary")
            onResult(emptyList())
            return
        }

        viewModelScope.launch {
            try {
                val snapshot = flightsCollection.get().await()
                val flights = snapshot.toObjects(Flight::class.java)
                onResult(flights)
            } catch (e: Exception) {
                Log.e("Firestore", "Error fetching flights: ${e.message}")
                onResult(emptyList())
            }
        }
    }


//------ACCOMMODATION------------------------------------
    fun addAccommodationToItinerary(
        travelerId: String?,
        tripId: String?,
        itineraryId: String?,
        accommodation: Accommodation,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        if (travelerId.isNullOrEmpty() || tripId.isNullOrEmpty() || itineraryId.isNullOrEmpty()) {
            Log.e("Firestore", "Invalid IDs - Traveler ID: $travelerId, Trip ID: $tripId, Itinerary ID: $itineraryId")
            onFailure(IllegalArgumentException("Invalid Firestore document reference: IDs cannot be empty"))
            return
        }

        val db = FirebaseFirestore.getInstance()
        val accommodationsCollection = db.collection("Travelers").document(travelerId)
            .collection("Trips").document(tripId)
            .collection("Itinerary").document(itineraryId)
            .collection("Accommodations")

        viewModelScope.launch {
            try {
                accommodationsCollection.document(accommodation.accommodationId).set(accommodation).await()
                Log.d("Firestore", "Accommodation added successfully!")
                onSuccess()
            } catch (e: Exception) {
                Log.e("Firestore", "Error adding accommodation: ${e.message}")
                onFailure(e)
            }
        }
    }

    fun getAccommodationsByItinerary(
        travelerId: String,
        tripId: String,
        itineraryId: String,
        onResult: (List<Accommodation>) -> Unit
    ) {
        val accommodationsCollection = FirebaseFirestore.getInstance()
            .collection("Travelers").document(travelerId)
            .collection("Trips").document(tripId)
            .collection("Itinerary").document(itineraryId)
            .collection("Accommodations")

        viewModelScope.launch {
            try {
                val snapshot = accommodationsCollection.get().await()
                val accommodations = snapshot.toObjects(Accommodation::class.java)
                onResult(accommodations)
            } catch (e: Exception) {
                Log.e("Firestore", "Error fetching accommodations: ${e.message}")
                onResult(emptyList())
            }
        }
    }



//-----ACTIVITY---------------------------------------
fun addActivityToItinerary(
    travelerId: String?,
    tripId: String?,
    itineraryId: String?,
    activity: Activity,
    onSuccess: () -> Unit,
    onFailure: (Exception) -> Unit
) {
    if (travelerId.isNullOrEmpty() || tripId.isNullOrEmpty() || itineraryId.isNullOrEmpty()) {
        Log.e("Firestore", "Invalid IDs - Traveler ID: $travelerId, Trip ID: $tripId, Itinerary ID: $itineraryId")
        onFailure(IllegalArgumentException("Invalid Firestore document reference: IDs cannot be empty"))
        return
    }

    val db = FirebaseFirestore.getInstance()
    val activitiesCollection = db.collection("Travelers").document(travelerId)
        .collection("Trips").document(tripId)
        .collection("Itinerary").document(itineraryId)
        .collection("Activities")

    viewModelScope.launch {
        try {
            activitiesCollection.document(activity.activityId).set(activity).await()
            Log.d("Firestore", "Activity added successfully!")
            onSuccess()
        } catch (e: Exception) {
            Log.e("Firestore", "Error adding activity: ${e.message}")
            onFailure(e)
        }
    }
}

    fun getActivitiesByItinerary(
        travelerId: String,
        tripId: String,
        itineraryId: String,
        onResult: (List<Activity>) -> Unit
    ) {
        val activitiesCollection = FirebaseFirestore.getInstance()
            .collection("Travelers").document(travelerId)
            .collection("Trips").document(tripId)
            .collection("Itinerary").document(itineraryId)
            .collection("Activities")

        viewModelScope.launch {
            try {
                val snapshot = activitiesCollection.get().await()
                val activities = snapshot.toObjects(Activity::class.java)
                onResult(activities)
            } catch (e: Exception) {
                Log.e("Firestore", "Error fetching activities: ${e.message}")
                onResult(emptyList())
            }
        }
    }

    fun deleteFlightFromItinerary(
        travelerId: String,
        tripId: String,
        itineraryId: String,
        flightId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val flightRef = FirebaseFirestore.getInstance()
            .collection("Travelers").document(travelerId)
            .collection("Trips").document(tripId)
            .collection("Itinerary").document(itineraryId)
            .collection("Flights").document(flightId)

        viewModelScope.launch {
            try {
                flightRef.delete().await()
                Log.d("Firestore", "Flight $flightId deleted successfully.")
                onSuccess()
            } catch (e: Exception) {
                Log.e("Firestore", "Failed to delete flight: ${e.message}")
                onFailure(e)
            }
        }
    }
    fun deleteAccommodationFromItinerary(
        travelerId: String,
        tripId: String,
        itineraryId: String,
        accommodationId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val accommodationRef = FirebaseFirestore.getInstance()
            .collection("Travelers").document(travelerId)
            .collection("Trips").document(tripId)
            .collection("Itinerary").document(itineraryId)
            .collection("Accommodations").document(accommodationId)

        viewModelScope.launch {
            try {
                accommodationRef.delete().await()
                Log.d("Firestore", "Accommodation $accommodationId deleted successfully.")
                onSuccess()
            } catch (e: Exception) {
                Log.e("Firestore", "Failed to delete accommodation: ${e.message}")
                onFailure(e)
            }
        }
    }

    fun deleteActivityFromItinerary(
        travelerId: String,
        tripId: String,
        itineraryId: String,
        activityId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val activityRef = FirebaseFirestore.getInstance()
            .collection("Travelers").document(travelerId)
            .collection("Trips").document(tripId)
            .collection("Itinerary").document(itineraryId)
            .collection("Activities").document(activityId)

        viewModelScope.launch {
            try {
                activityRef.delete().await()
                Log.d("Firestore", "Activity $activityId deleted successfully.")
                onSuccess()
            } catch (e: Exception) {
                Log.e("Firestore", "Failed to delete activity: ${e.message}")
                onFailure(e)
            }
        }
    }

    fun getFlightCountForItinerary(
        travelerId: String,
        tripId: String,
        itineraryId: String,
        onResult: (Int) -> Unit
    ) {
        val flightsRef = FirebaseFirestore.getInstance()
            .collection("Travelers").document(travelerId)
            .collection("Trips").document(tripId)
            .collection("Itinerary").document(itineraryId)
            .collection("Flights")

        viewModelScope.launch {
            try {
                val snapshot = flightsRef.get().await()
                onResult(snapshot.size())
            } catch (e: Exception) {
                Log.e("Firestore", "Error fetching flight count: ${e.message}")
                onResult(0)
            }
        }
    }


    // POST FUNCTIONS----------------------------------------------------------
    fun fetchPosts(travelerId: String, selectedCity: String? = null) {
        viewModelScope.launch {
            try {
                val query = if (selectedCity == null) {
                    db.collection("Travelers").document(travelerId)
                        .collection("Posts")
                        .get()
                        .await()
                } else {
                    db.collection("Travelers").document(travelerId)
                        .collection("Posts")
                        .whereEqualTo("city", selectedCity)
                        .get()
                        .await()
                }

                val allPosts = query.documents.mapNotNull { doc ->
                    doc.toObject(Post::class.java)
                }

                _postsLiveData.value = allPosts // Update to refresh UI

            } catch (e: Exception) {
                Log.e("Firestore", "Error fetching posts: ${e.message}")
            }
        }
    }

        // Add Post
        fun addPost(travelerId: String, post: Post) {
            viewModelScope.launch {
                try {
                    val postRef = db.collection("Travelers").document(travelerId)
                        .collection("Posts").document(post.postId)

                    postRef.set(post).await()
                   // fetchPosts(travelerId)
                    Log.d("Firestore", "Post successfully added!")
                } catch (e: Exception) {
                    Log.e("Firestore", "Error adding post: $e")
                }
            }
        }



    // Fetch all posts from all travelers
    fun getAllPosts(currentTravelerId: String, city: String?, onResult: (List<Post>) -> Unit) {
        viewModelScope.launch {
            try {
                val allPosts = mutableListOf<Post>()

                val travelersSnapshot = db.collection("Travelers").get().await()

                for (travelerDoc in travelersSnapshot.documents) {
                    val travelerId = travelerDoc.id
                    //if (travelerId == currentTravelerId) continue // Exclude current user

                    val postsSnapshot = db.collection("Travelers").document(travelerId)
                        .collection("Posts").get().await()

                    val posts = postsSnapshot.documents.mapNotNull { doc ->
                        val post = doc.toObject(Post::class.java)
                        post?.let {
                            it.copy( // deep copy
                                postId = it.postId,
                                travelerId = it.travelerId,
                                postText = it.postText,
                                city = it.city,
                                imageRes = it.imageRes,
                                totalComments = it.totalComments,
                                totalLikes = it.totalLikes,
                                likedBy = ArrayList(it.likedBy),
                                publicationDate = it.publicationDate
                            )
                        }
                    }

                    if (city != null) {
                        allPosts.addAll(posts.filter { it.city == city })
                    } else {
                        allPosts.addAll(posts)
                    }
                }
                onResult(allPosts)
            } catch (e: Exception) {
                Log.e("Firestore", "Error getting posts: ${e.message}")
                onResult(emptyList())
            }
        }
    }


    // Fetch posts by traveler (My Posts)
    fun getPostsByTraveler(travelerId: String, onResult: (List<Post>) -> Unit) {
        viewModelScope.launch {
            try {
                val result = db.collection("Travelers").document(travelerId)
                    .collection("Posts")
                    .whereEqualTo("travelerId", travelerId)
                    .get().await()

                val posts = result.documents.mapNotNull { doc ->
                    val post = doc.toObject(Post::class.java)
                    post?.let {
                        it.copy(  //deep copy
                            postId = it.postId,
                            travelerId = it.travelerId,
                            postText = it.postText,
                            city = it.city,
                            imageRes = it.imageRes,
                            totalComments = it.totalComments,
                            totalLikes = it.totalLikes,
                            likedBy = ArrayList(it.likedBy),
                            publicationDate = it.publicationDate
                        )
                    }
                }
                onResult(posts)
            } catch (e: Exception) {
                Log.e("Firestore", "Error getting user posts: ${e.message}")
                onResult(emptyList())
            }
        }
    }


    fun getPostOwnerId(postId: String, onResult: (String?) -> Unit) {
        viewModelScope.launch {
            try {
                val travelersSnapshot = db.collection("Travelers").get().await()
                for (travelerDoc in travelersSnapshot.documents) {
                    val postsRef = travelerDoc.reference.collection("Posts").document(postId)
                    val postSnapshot = postsRef.get().await()

                    if (postSnapshot.exists()) {
                        val postOwnerId = travelerDoc.id
                        onResult(postOwnerId)
                        return@launch
                    }
                }
                onResult(null)  // Post not found under any traveler
            } catch (e: Exception) {
                Log.e("Firestore", "Error retrieving post owner ID: ${e.message}")
                onResult(null)
            }
        }
    }



    fun deletePost(travelerId: String, postId: String) {
        viewModelScope.launch {
            try {
                db.collection("Travelers").document(travelerId)
                    .collection("Posts").document(postId)
                    .delete().await()
                Log.d("Firestore", "Post successfully deleted!")

            } catch (e: Exception) {
                Log.e("Firestore", "Error deleting post: ${e.message}")
            }
        }
    }


    // Like a post
    fun likePost(travelerId: String, postOwnerId: String, postId: String) {
        viewModelScope.launch {
            try {
                val postRef = db.collection("Travelers").document(postOwnerId)
                    .collection("Posts").document(postId)

                db.runTransaction { transaction ->
                    val snapshot = transaction.get(postRef)
                    val post = snapshot.toObject(Post::class.java)

                    if (post != null) {
                        val likedBy = post.likedBy.toMutableList()

                        if (!likedBy.contains(travelerId)) { // Only like if the user hasn't already liked the post
                            likedBy.add(travelerId)
                            transaction.update(postRef, "likedBy", likedBy)
                            transaction.update(postRef, "totalLikes", likedBy.size)
                        }
                    }
                }.await()
                Log.d("Firestore", "Post liked successfully!")
            } catch (e: Exception) {
                Log.e("Firestore", "Error liking post: ${e.message}")
            }
        }
    }

    // Unlike a post
    fun unlikePost(travelerId: String, postOwnerId: String, postId: String) {
        viewModelScope.launch {
            try {
                val postRef = db.collection("Travelers").document(postOwnerId)
                    .collection("Posts").document(postId)

                db.runTransaction { transaction ->
                    val snapshot = transaction.get(postRef)
                    val post = snapshot.toObject(Post::class.java)

                    if (post != null) {
                        val likedBy = post.likedBy.toMutableList()

                        if (likedBy.contains(travelerId)) { // Only unlike if the user has liked the post
                            likedBy.remove(travelerId)
                            transaction.update(postRef, "likedBy", likedBy)
                            transaction.update(postRef, "totalLikes", likedBy.size)
                        }
                    }
                }.await()
                Log.d("Firestore", "Post unliked successfully!")
            } catch (e: Exception) {
                Log.e("Firestore", "Error unliking post: ${e.message}")
            }
        }
    }



    // Get Traveler Name by ID
    fun getNameByID(travelerId: String, onResult: (String?) -> Unit) {
        viewModelScope.launch {
            try {
                val result = db.collection("Travelers").document(travelerId)
                    .get().await()

                val name = result.getString("name")
                onResult(name)
            } catch (e: Exception) {
                Log.e("Firestore", "Error fetching traveler name: ${e.message}")
                onResult(null)
            }
        }
    }


    fun addComment(postOwnerId: String, postId: String, comment: Comment, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val postRef = db.collection("Travelers").document(postOwnerId)
                    .collection("Posts").document(postId)

                val postSnapshot = postRef.get().await()
                if (!postSnapshot.exists() || postSnapshot.data.isNullOrEmpty()) {
                    Log.e(
                        "Firestore",
                        "Post document is empty or not properly initialized: $postId"
                    )
                    onComplete(false)
                    return@launch
                }

                val commentsRef = postRef.collection("Comments").document(comment.commentId)
                commentsRef.set(comment).await()

                // Increment comment count
                postRef.update("totalComments", FieldValue.increment(1)).await()

                Log.d("Firestore", "Comment added successfully!")
                onComplete(true)
            } catch (e: Exception) {
                Log.e("Firestore", "Error adding comment: ${e.message}")
                onComplete(false)
            }
        }
    }

    fun deleteComment(postOwnerId: String, postId: String, commentId: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val postRef = db.collection("Travelers").document(postOwnerId)
                    .collection("Posts").document(postId)

                // Check if the post exists
                val postSnapshot = postRef.get().await()
                if (!postSnapshot.exists()) {
                    Log.e("Firestore", "Error! Post not found. Cannot delete comment.")
                    onComplete(false)
                    return@launch
                }

                val commentRef = postRef.collection("Comments").document(commentId)
                commentRef.delete().await()

                // Decrement comment count only if it's greater than zero
                postRef.get().addOnSuccessListener { snapshot ->
                    val currentCount = snapshot.getLong("totalComments") ?: 0L
                    if (currentCount > 0) {
                        postRef.update("totalComments", FieldValue.increment(-1))
                    }
                }
                //postRef.update("totalComments", FieldValue.increment(-1)).await()
                Log.d("Firestore", "Comment deleted successfully!")
                onComplete(true)
            } catch (e: Exception) {
                Log.e("Firestore", "Error deleting comment: ${e.message}")
                onComplete(false)
            }
        }
    }




    fun getCommentsByPost(postId: String, onResult: (List<Comment>) -> Unit) {
        viewModelScope.launch {
            try {
                val travelersCollection = db.collection("Travelers")
                val commentsList = mutableListOf<Comment>()

                // Search for the postId across all travelers
                val travelersSnapshot = travelersCollection.get().await()
                for (traveler in travelersSnapshot.documents) {
                    val postsRef = traveler.reference.collection("Posts").document(postId)
                    val commentsRef = postsRef.collection("Comments")

                    try {
                        val snapshot = commentsRef.get().await()
                        val comments = snapshot.documents.mapNotNull { it.toObject(Comment::class.java) }
                        commentsList.addAll(comments)
                    } catch (e: Exception) {
                        Log.e("Firestore", "Error fetching comments for postId $postId: ${e.message}")
                    }
                }

                onResult(commentsList)
            } catch (e: Exception) {
                Log.e("Firestore", "Error fetching travelers: ${e.message}")
                onResult(emptyList())
            }
        }
    }

    //-------------Budget----------------------

    fun getBudget(
        travelerId: String,
        tripId: String,
        itineraryId: String,
        callback: (Budget) -> Unit
    ) {
        val docRef = db
            .collection("Travelers").document(travelerId)
            .collection("Trips").document(tripId)
            .collection("Itinerary").document(itineraryId)
            .collection("Budget").document("Main")

        docRef.get().addOnSuccessListener { doc ->
            val budget = doc.toObject(Budget::class.java)
            if (budget != null) {
                callback(budget.copy(budgetId = "Main")) //Main because each Trip has only ONE budget
            } else {
                callback(Budget(budgetId = "Main", tripId = tripId, itineraryId = itineraryId))
            }
        }.addOnFailureListener {
            callback(Budget(budgetId = "Main", tripId = tripId, itineraryId = itineraryId))
        }
    }

    fun addBudget(
        travelerId: String,
        tripId: String,
        itineraryId: String,
        amount: Float,
        onSuccess: () -> Unit
    ) {
        val docRef = db
            .collection("Travelers").document(travelerId)
            .collection("Trips").document(tripId)
            .collection("Itinerary").document(itineraryId)
            .collection("Budget").document("Main")

        val budget = Budget(
            budgetId = "Main",
            tripId = tripId,
            itineraryId = itineraryId,
            budget = amount.toDouble(),
            netBalance = amount.toDouble(),
            totalExpenses = 0.0
        )

        docRef.set(budget)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener {
                it.printStackTrace()
            }
    }


    fun getExpenses(
        travelerId: String,
        tripId: String,
        itineraryId: String,
        callback: (List<Expense>) -> Unit
    ) {
        val colRef = db
            .collection("Travelers").document(travelerId)
            .collection("Trips").document(tripId)
            .collection("Itinerary").document(itineraryId)
            .collection("Budget").document("Main")
            .collection("Expenses")

        colRef.get().addOnSuccessListener { snapshot ->
            val expenses = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Expense::class.java)?.copy(expenseId = doc.id)
            }
            callback(expenses)
        }.addOnFailureListener {
            callback(emptyList())
        }
    }

    fun addExpense(
        travelerId: String,
        tripId: String,
        itineraryId: String,
        name: String,
        amount: Float,
        onSuccess: () -> Unit
    ) {
        val expensesRef = db
            .collection("Travelers").document(travelerId)
            .collection("Trips").document(tripId)
            .collection("Itinerary").document(itineraryId)
            .collection("Budget").document("Main")
            .collection("Expenses")

        val newExpense = Expense(
            name = name,
            amount = amount
        )

        expensesRef.add(newExpense)
            .addOnSuccessListener {
                updateBudgetTotals(travelerId, tripId, itineraryId) {
                    onSuccess()
                }
            }
            .addOnFailureListener { it.printStackTrace() }
    }



    private fun updateBudgetTotals(
        travelerId: String,
        tripId: String,
        itineraryId: String,
        onComplete: () -> Unit = {}
    ) {
        val budgetRef = db
            .collection("Travelers").document(travelerId)
            .collection("Trips").document(tripId)
            .collection("Itinerary").document(itineraryId)
            .collection("Budget").document("Main")

        val expensesRef = budgetRef.collection("Expenses")

        expensesRef.get().addOnSuccessListener { snapshot ->
            val totalExpenses = snapshot.documents
                .filterNot { doc ->
                    val name = doc.getString("name")?.lowercase() ?: ""
                    name.contains("budget")
                }
                .sumOf { it.getDouble("amount") ?: 0.0 }

            val totalExpensesFloat = totalExpenses.toFloat()

            budgetRef.get().addOnSuccessListener { doc ->
                val currentBudget = doc.getDouble("budget")?.toFloat() ?: 0f
                val netBalance = currentBudget - totalExpensesFloat

                budgetRef.update(
                    mapOf(
                        "totalExpenses" to totalExpensesFloat,
                        "netBalance" to netBalance
                    )

                ).addOnSuccessListener {
                    onComplete()
                }.addOnFailureListener {
                    it.printStackTrace()
                    onComplete()
                }
            }.addOnFailureListener {
                it.printStackTrace()
                onComplete()
            }
        }.addOnFailureListener {
            it.printStackTrace()
            onComplete()
        }
    }




    fun updateBudgetAmount(
        travelerId: String,
        tripId: String,
        itineraryId: String,
        newAmount: Float,
        onSuccess: () -> Unit
    ) {
        val docRef = db
            .collection("Travelers").document(travelerId)
            .collection("Trips").document(tripId)
            .collection("Itinerary").document(itineraryId)
            .collection("Budget").document("Main")

        docRef.update("budget", newAmount)
            .addOnSuccessListener {
                updateBudgetTotals(travelerId, tripId, itineraryId)
                onSuccess()
            }
            .addOnFailureListener {
                it.printStackTrace()
            }
    }

//    fun deleteExpense(
//        travelerId: String,
//        tripId: String,
//        itineraryId: String,
//        expenseId: String,
//        onSuccess: () -> Unit
//    ) {
//        val expenseRef = db
//            .collection("Travelers").document(travelerId)
//            .collection("Trips").document(tripId)
//            .collection("Itinerary").document(itineraryId)
//            .collection("Budget").document("Main")
//            .collection("Expenses").document(expenseId)
//
//        expenseRef.delete()
//            .addOnSuccessListener {
//                updateBudgetTotals(travelerId, tripId, itineraryId) {
//                    onSuccess()
//                }
//            }
//            .addOnFailureListener {
//                it.printStackTrace()
//            }
//    }

    fun deleteExpense(
        travelerId: String,
        tripId: String,
        itineraryId: String,
        expenseId: String,
        onSuccess: () -> Unit
    ) {
        val budgetRef = db
            .collection("Travelers").document(travelerId)
            .collection("Trips").document(tripId)
            .collection("Itinerary").document(itineraryId)
            .collection("Budget").document("Main")

        val expenseRef = budgetRef.collection("Expenses").document(expenseId)

        expenseRef.get().addOnSuccessListener { doc ->
            val expenseName = doc.getString("name")?.lowercase() ?: ""
            val expenseAmount = doc.getDouble("amount") ?: 0.0

            expenseRef.delete().addOnSuccessListener {
                // If its budget entry, subtract its value from the budget total
                if (expenseName.contains("budget")) {
                    budgetRef.get().addOnSuccessListener { budgetDoc ->
                        val currentBudget = budgetDoc.getDouble("budget") ?: 0.0
                        val updatedBudget = currentBudget - expenseAmount
                        budgetRef.update("budget", updatedBudget)
                            .addOnSuccessListener {
                                updateBudgetTotals(travelerId, tripId, itineraryId) {
                                    onSuccess()
                                }
                            }
                    }
                } else {
                    updateBudgetTotals(travelerId, tripId, itineraryId) {
                        onSuccess()
                    }
                }
            }
        }
    }



    fun getHistory(travelerId: String, onComplete: (List<String>) -> Unit){
        viewModelScope.launch {

            try {
                val history = repository.getHistory(travelerId = travelerId)
                onComplete(history)
            }
            catch (e: Exception) {
                Log.e("HistoryFetch", "Error: ${e.message}")
                onComplete(emptyList()) // fallback
            }

        }
    }

    fun getHistoryAndPreferences(travelerId: String,
                                 onComplete: (List<String>,List<String>) -> Unit){
        viewModelScope.launch {
            try {
                val history = repository.getHistory(travelerId)
                val preferences = repository.getPreferences(travelerId)
                onComplete(history,preferences)

            }catch (e: Exception){
                Log.e("HistoryAndPreferencesFetch", "Error: ${e.message}")
                onComplete(emptyList(), emptyList())
            }
        }
    }

  // this bellow is what used to view history in Log

//    val hasLoaded = remember { mutableStateOf(false) }
//    LaunchedEffect(Unit) {
//        if (!hasLoaded.value) {
//            viewModel.getHistory(travelerId) { result ->
//                Log.d("HistoryTest", "Combined history string: ${result["rawHistory"]}")
//            }
//            hasLoaded.value = true
//        }
//    }


    suspend fun getCurrentTrips(travelerId: String): List<Trip> {
        return try {
            val result = db.collection("Travelers").document(travelerId)
                .collection("Trips").get().await()

            val allTrips = result.documents.mapNotNull { doc ->
                val trip = doc.toObject(Trip::class.java)
                trip?.copy(itineraryId = doc.getString("itineraryId") ?: "")
            }

            val today = Calendar.getInstance().time
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

            allTrips.filter { trip ->
                try {
                    val start = dateFormat.parse(trip.startDate)
                    val end = dateFormat.parse(trip.endDate)
                    start != null && end != null && !start.after(today) && !end.before(today)
                } catch (e: Exception) {
                    false
                }
            }
        } catch (e: Exception) {
            Log.e("Firestore", "Error fetching current trips: ${e.message}")
            emptyList()
        }
    }

    fun saveTravelerPreferences(
        travelerId: String,
        preferences: Map<String, String>,
        onSuccess: () -> Unit = {},
        onFailure: (String) -> Unit = {}
    ) {
        val profileRef = db.collection("Travelers")
            .document(travelerId)
            .collection("Profile")
            .document("Main")

        profileRef.set(
            mapOf(
                "profileId" to "Main",
                "travelerId" to travelerId,
                "preferenceList" to preferences
            )
        ).addOnSuccessListener {
            android.os.Handler(android.os.Looper.getMainLooper()).post {
                onSuccess()
            }
        }.addOnFailureListener {
            onFailure(it.message ?: "Failed")
        }
    }


}

