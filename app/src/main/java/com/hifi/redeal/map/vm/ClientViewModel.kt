package com.hifi.redeal.map.vm

import android.graphics.Color
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hifi.redeal.R
import com.hifi.redeal.map.model.ClientDataClass
import com.hifi.redeal.map.model.ScheduleDataClass
import com.hifi.redeal.map.repository.ClientRepository
import com.hifi.redeal.map.repository.MapRepository
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.label.LabelOptions
import com.kakao.vectormap.label.LabelStyle
import com.kakao.vectormap.label.LabelStyles
import com.kakao.vectormap.label.LabelTextStyle
import com.kakao.vectormap.mapwidget.InfoWindowOptions
import com.kakao.vectormap.mapwidget.component.GuiImage
import com.kakao.vectormap.mapwidget.component.GuiLayout
import com.kakao.vectormap.mapwidget.component.GuiText
import com.kakao.vectormap.mapwidget.component.Orientation
import java.text.SimpleDateFormat
import java.util.Calendar

class ClientViewModel : ViewModel() {
    var clientDataListByKeyWord = MutableLiveData<MutableList<ClientDataClass>>()
    var clientDataListAll = MutableLiveData<MutableList<ClientDataClass>>()
    var currentAddress = MutableLiveData<LatLng>()
    var clientDataListLabel = MutableLiveData<MutableList<InfoWindowOptions>>()
    val selectedButtonId = MutableLiveData<Int>()

    init {
        clientDataListByKeyWord.value = mutableListOf<ClientDataClass>()
        selectedButtonId.value = R.id.mapBottomSheetTabAll
    }

    fun getClientListByKeyword(userIdx: String, keyword: String) {

        val tempList = mutableListOf<ClientDataClass>()

        ClientRepository.getClientListByUser(userIdx) {
            for (snapshot in it.result.documents) {
                val clientData = snapshot.toObject(ClientDataClass::class.java)
                Log.d("검색 테스트1", snapshot.toObject(ClientDataClass::class.java).toString())
                if (clientData!=null) {
                    val clientName = clientData.clientName
                    val managerName = clientData.clientManagerName

                    // "clientName" 또는 "managerName" 중 하나라도 키워드를 포함하면 tempList에 추가
                    if (clientName.contains(keyword) || managerName.contains(keyword)) {
                        tempList.add(clientData)
                    }
                    Log.d("검색 테스트", tempList.toString())
                }
            }
            clientDataListByKeyWord.value = tempList
        }
    }

    fun getClientListAll(userIdx: String) {
        val tempList = mutableListOf<ClientDataClass>()
        ClientRepository.getClientListByUser(userIdx) {
            for (snapshot in it.result.documents) {
                Log.d("거래처처 테스트1", snapshot.toObject(ClientDataClass::class.java).toString())
                var item = snapshot.toObject(ClientDataClass::class.java)
                tempList.add(item!!)
            }
            clientDataListAll.value = tempList
        }

    }

    fun getClientListTodayVisit(userIdx: String) {
        val clientTempList = mutableListOf<ClientDataClass>()
        ClientRepository.getClientListByUser(userIdx) {
            for (snapshot in it.result.documents) {
                Log.d("거래처정 테스트1", snapshot.toObject(ClientDataClass::class.java).toString())
                var item = snapshot.toObject(ClientDataClass::class.java)
                ClientRepository.getVisitScheduleListByClientAndUser(userIdx, item!!.clientIdx) {
                    Log.d("거래처정 테스트2", item.clientIdx.toString())
                    var i = 0;
                    while (i <= it.result.documents.size - 1) {
                        val snapshot = it.result.documents.get(i)
                        Log.d("거래처정 테스트3", snapshot.toObject(ScheduleDataClass::class.java).toString())
                        val dateFormat = SimpleDateFormat("yyyy.MM.dd")
                        val tempSnapShotDate =
                            dateFormat.format(snapshot.getDate("scheduleDeadlineTime"))
                        val today = dateFormat.format(Calendar.getInstance().time)
                        Log.d("일정정1", tempSnapShotDate)
                        Log.d("일정정2", today)
                        if (tempSnapShotDate.equals(today)
                        ) {
                            Log.d(
                                "일정정3",
                                snapshot.toObject(ScheduleDataClass::class.java).toString()
                            )
                            clientTempList.add(item)
                            i++
                        } else {
                            i++
                        }
                        Log.d("일정정4", clientTempList.toString())
                    }
                    Log.d("일정정5", clientTempList.toString())
                    clientDataListAll.value = clientTempList
                }
            }
        }
    }

    fun getClientListBookMark(userIdx: String){
        val tempList = mutableListOf<ClientDataClass>()
        ClientRepository.getClientListByUser(userIdx) {
            for (snapshot in it.result.documents) {
                Log.d("거래처처처 테스트1", snapshot.toObject(ClientDataClass::class.java).toString())
                var item = snapshot.toObject(ClientDataClass::class.java)
                tempList.add(item!!)
            }
            clientDataListAll.value = tempList.filter { it.isBookmark == true } as MutableList<ClientDataClass>
        }
    }


    fun getClientListLabel(userIdx: String) {
        val tempList = mutableListOf<ClientDataClass>()
        val labelList = mutableListOf<InfoWindowOptions>()
        ClientRepository.getClientListByUser(userIdx) {
            for (snapshot in it.result.documents) {
                Log.d("거래처 테스트1", snapshot.toObject(ClientDataClass::class.java).toString())
                var item = snapshot.toObject(ClientDataClass::class.java)
                tempList.add(item!!)
            }
            Log.d("거래처 테스트2", tempList.toString())
            for(c in tempList) {
                Log.d("거래처 테스트3",c.clientAddress.toString())
                if(c.clientAddress==""){
                    continue
                }
                // 괄호와 괄호 안의 글자를 삭제하기 위한 정규 표현식
                val regex = "\\([^)]*\\)"

                val clientAddr = c.clientAddress!!.replace(regex.toRegex(), "")
                Log.d("거래처 테스트4",clientAddr)
                MapRepository.searchAddr(clientAddr) {
                    Log.d("거래처 테스트5",it.toString())
                    if (it!!.isEmpty()) {
                        return@searchAddr
                    }
                    val lat = it!!.get(0).y.toDouble()
                    val long = it!!.get(0).x.toDouble()
                    val latLng = LatLng.from(lat, long)

                    val body = GuiLayout(Orientation.Horizontal)
                    body.setPadding(0, -20, 0, -20)

                    val bgImage = GuiImage(R.drawable.window_info_bg, true)
                    bgImage.setFixedArea(39, 39, 39, 39)
                    body.setBackground(bgImage)

                    val text = GuiText(c.clientName)
                    text.textSize = 30
                    text.textColor = Color.DKGRAY
                    body.addView(text)

                    val options =  InfoWindowOptions.from(latLng)
                    options.setBody(body)
                    options.setBodyOffset(0f, -41f)
                    val infoWindowOptions = options.setTail(GuiImage(R.drawable.window_info_tail, false))


                    val style = LabelStyles.from(
                        LabelStyle.from(R.drawable.blue_marker).setTextStyles(
                            LabelTextStyle.from(30, R.color.primary20, 1, Color.WHITE)
                        )
                    );

                    labelList.add(
                        infoWindowOptions
                    )
                    clientDataListLabel.value = labelList
                    Log.d("라벨3", clientDataListLabel.value.toString())
                }
            }


        }


    }




    fun resetClientListByKeyword() {
        clientDataListByKeyWord.value = mutableListOf<ClientDataClass>()
    }


    fun setSelectedButton(buttonId: Int) {
        selectedButtonId.value = buttonId
    }


}