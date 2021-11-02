package com.changs.publictoilet

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.changs.publictoilet.data.Toilet

import com.changs.publictoilet.databinding.ActivityMapsBinding
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MapsActivity : BaseActivity(), OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    override fun onInfoWindowClick(mMap: Marker) {
        if(mMap.tag != null) {
            var uri = mMap.tag as String
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
            startActivity(intent)
        }
    }

    override fun permissionGranted(requestCode: Int) {
        startProcess()
    }
    override fun permissionDenied(requestCode: Int) {
        Toast.makeText(this, "권한 승인이 필요합니다.", Toast.LENGTH_LONG).show()
    }
    //위치 권한이 승인 되면 해당 메서드에서 구글 지도를 준비하는 작업을 집행
    fun startProcess() {
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

    }
    //위치를 처리하기 위한 변수
    private lateinit var fusedLocationClient: FusedLocationProviderClient //위칫값을 사용하기 위함
    private lateinit var locationCallback: LocationCallback // 위칫값 요청에 대한 갱신 정보를 받는 데 필요

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)


        //앱에서 사용할 권한을 변수에 저장하고, 권한을 요청하는 코드
        val permissions = arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION)

        requirePermissions(permissions, 999)

    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val SEOUL = LatLng(37.56, 126.97)
        val cameraPosition = CameraPosition.Builder()
            .target(SEOUL)
            .zoom(13.0f)
            .build()
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition)) //초기 위치 설정

        mMap.uiSettings.setMapToolbarEnabled(false) // 툴바 사용 중지

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        updateLocation()

        loadToilets()

        mMap.setOnInfoWindowClickListener(this) //정보창 클릭 이벤트

    }

    @SuppressLint("MissingPermission") //해당 코드를 체크하지 않아도 된다는 의미의 애너테이션
    fun updateLocation() {
        val locationRequest = LocationRequest.create() //위치 정보를 요청할 정확도와 주기를 설정할 변수
        locationRequest.run {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY //정확도
            interval = 1000 //1초 주기
        }
        // 1초에 한 번씩 변화된 위치 정보가 onLocationResult()으로 전달됨
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult?.let {
                    for(location in it.locations) {
                        Log.d("Location", "${location.latitude} , ${location.longitude}")
                        setLastLocation(location) //onLocationResult()가 반환받은 정보에서 위치 정보를 setLastLocation으로 전달함

                    }
                }
            }
        }
        //앞에서 생성한 2개와 함께 루퍼 정보를 넘겨줌
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
    }



    var putMarker: Marker? = null //사용자 위치 마커를 매번 지웠다 다시 생성하기 위해 마커 변수 생성

    //위치 정보를 받아서 마커를 그리고 화면을 이동하는 setLastLocation() 생성
    fun setLastLocation(lastLocation: Location) {
        var bitmapDrawable: BitmapDrawable //내 위치 마커 아이콘 변경하기
        // 롤리팝 버전 이전과 버전 이후에서 동작하는 코드가 다르므로 버전 처리

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            bitmapDrawable = getDrawable(R.drawable.aim) as BitmapDrawable
        }else {
            bitmapDrawable = resources.getDrawable(R.drawable.aim) as BitmapDrawable
        }
        //아이콘 크기 조절
        var scaledBitmap = Bitmap.createScaledBitmap(bitmapDrawable.bitmap, 80, 80, false)

        var discriptor = BitmapDescriptorFactory.fromBitmap(scaledBitmap)

        //전달 받은 위치 정보로 좌표를 생성
        val LATLNG = LatLng(lastLocation.latitude, lastLocation.longitude)

        //좌표로 마커를 생성
        var markerOptions = MarkerOptions()
            .position(LATLNG)
            .position(LATLNG)
            .icon(discriptor)
        //카메라 위치를 현재 위치로 세팅
        val cameraPosition = CameraPosition.Builder()
            .target(LATLNG)
            .zoom(15.0f)
            .build()




       
        putMarker?.remove() //마지막으로 생성된 마커(내 위치 마커) 제거
        putMarker = mMap.addMarker(markerOptions)
        mMap.setOnMapClickListener {
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))//마커와 함께 지도에 반영
        }

    }


    //앞에서 정의한 인터페이스를 적용하고 데이터를 불러오는 코드
    fun loadToilets() {
        val retrofit = Retrofit.Builder() //도메인 주소와 JSON 컨버터를 설정해서 레트로핏을 생성
            .baseUrl(SeoulOpenApi.DOMAIN)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val seoulOpenService = retrofit.create(SeoulOpenService::class.java) //인터페이스를 실행 가능한 서비스 객체로 변환
        seoulOpenService//인터페이스에 정의된 getToilet() 메서드에 'API_KEY'를 입력하고, enqueue() 메서드를 호출해서 서버에 요청
            .getToilet(SeoulOpenApi.API_KEY)
            .enqueue(object: Callback<Toilet> {
                override fun onResponse(call: Call<Toilet>, response: Response<Toilet>) {
                    //서버에서 데이터를 정상적으로 받았다면 지도에 마커를 표시하는 메서드 호출
                    showToilets(response.body() as Toilet)
                }

                override fun onFailure(call: Call<Toilet>, t: Throwable) {//서버 요청이 실패했을 경우 토스트 던지기
                    Toast.makeText(baseContext, "서버에서 데이터를 가져올 수 없습니다.", Toast.LENGTH_LONG).show()
                }
            })
    }

    fun showToilets(toilet: Toilet){
        var bitmapDrawable: BitmapDrawable //마커 아이콘 변경하기
        // 롤리팝 버전 이전과 버전 이후에서 동작하는 코드가 다르므로 버전 처리

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            bitmapDrawable = getDrawable(R.drawable.bathroom) as BitmapDrawable
        }else {
            bitmapDrawable = resources.getDrawable(R.drawable.bathroom) as BitmapDrawable
        }
        //아이콘 크기 조절
        var scaledBitmap = Bitmap.createScaledBitmap(bitmapDrawable.bitmap, 50, 50, false)

        var discriptor = BitmapDescriptorFactory.fromBitmap(scaledBitmap)

        //좌표로 마커를 생성
        val latLngBounds = LatLngBounds.Builder()
        for (i in toilet.SearchPublicToiletPOIService.row) {
            val position = LatLng(i.Y_WGS84, i.X_WGS84)
            val marker = MarkerOptions()
                .position(position)
                .icon(discriptor)
                .title(i.FNAME)
                .snippet(i.ANAME)
            var obj = mMap.addMarker(marker)
            obj.tag = "geo:${i.Y_WGS84}, ${i.X_WGS84}"

            latLngBounds.include(marker.position)
        }

    }

}