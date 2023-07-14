package com.harera.edit_product

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.harera.common.base.BaseViewModel
import com.harera.common.local.UserDataStore
import com.harera.model.model.Product
import com.harera.repository.abstraction.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditProductViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    userDataStore: UserDataStore,
) : BaseViewModel(userDataStore) {
    private val _productId: MutableLiveData<String> = MutableLiveData()
    val productId: LiveData<String> = _productId

    private val _operationCompleted: MutableLiveData<Boolean> = MutableLiveData()
    val operationCompleted: LiveData<Boolean> = _operationCompleted

    private val _image: MutableLiveData<Bitmap> = MutableLiveData()
    val image: LiveData<Bitmap> = _image

    private val _imageUrl: MutableLiveData<String> = MutableLiveData()
    val imageUrl: LiveData<String> = _imageUrl

    private val _product: MutableLiveData<Product> =
        MutableLiveData()
    val product: LiveData<Product> = _product

    fun setImage(imageBitmap: Bitmap) {
        _image.value = imageBitmap
    }

    private suspend fun uploadProductImage() {
        productRepository
            .uploadProductImage(
                product.value!!.productId,
                image.value!!,
            )
            .onSuccess {
                setImageUrl(it)
            }
            .onFailure {
                handleException(it)
            }
    }

    private fun setImageUrl(imageUrl: String) {
        _imageUrl.postValue(imageUrl)
    }

    fun updateProduct() {
        if (_image.value == null) {
            _operationCompleted.value = true
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            uploadProductImage()
            updateProductImage()
        }
    }

    private suspend fun updateProductImage() {
        productRepository
            .uploadProductImage(
                productId = product.value!!.productId,
                productImageUrl = imageUrl.value!!,
            )
            .onSuccess {
                _operationCompleted.postValue(it)
            }
            .onFailure {
                handleException(it)
            }
    }

    suspend fun getProduct() {
        productRepository
            .getProduct(productId = productId.value!!)
            .onSuccess {
                _product.postValue(it)
            }
            .onFailure {
                handleException(it)
            }
    }

    fun setProductId(it: String) {
        _productId.postValue(it)
    }
}
