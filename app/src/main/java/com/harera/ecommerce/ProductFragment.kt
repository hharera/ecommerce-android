package com.harera.product

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.harera.common.utils.navigation.Arguments.PRODUCT_ID
import com.harera.components.product.ProductsAdapter
import com.harera.model.model.Product
import com.harera.product.databinding.FragmentProductViewBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.text.Typography.pound

class ProductFragment : BaseFragment() {
    private val productViewModel: ProductViewModel by viewModels()
    private lateinit var bind: FragmentProductViewBinding
    private lateinit var productsAdapter: ProductsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            val productId = it.getString(PRODUCT_ID)!!
            productViewModel.setProductId(productId)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        bind = FragmentProductViewBinding.inflate(layoutInflater)
        productsAdapter = ProductsAdapter(
            onProductClicked = {

            }
        )
        return bind.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupProductsAdapter()

        lifecycleScope.launch(Dispatchers.IO) {
            productViewModel.getProduct()
        }

        setupObservers()
    }

    private fun setupObservers() {
        productViewModel.loading.observe(viewLifecycleOwner) {
            handleLoading(it)
        }

        productViewModel.product.observe(viewLifecycleOwner) {
            handleSuccess()
            updateUI(it)

            lifecycleScope.launch(Dispatchers.IO) {
                productViewModel.getWishState()
                productViewModel.getCartState()
            }
        }

        productViewModel.exception.observe(viewLifecycleOwner) {
            handleError(it)
        }

        productViewModel.wishState.observe(viewLifecycleOwner) {
            updateWishIcon(state = it)
            handleSuccess()
        }

        productViewModel.products.observe(viewLifecycleOwner) {
            updateProducts(it)
        }

        productViewModel.cartItem.observe(viewLifecycleOwner) {
            bind.cart.isExpanded = true
        }

        connectionLiveData.observe(viewLifecycleOwner) {
            productViewModel.updateConnectivity(it)
        }
    }

    private fun updateProducts(products: List<Product>) {
        productsAdapter.setProducts(productList = products)
    }

    private fun setupProductsAdapter() {
        bind.products.adapter = productsAdapter
        bind.products.layoutManager = GridLayoutManager(context, 2)
    }

    private fun updateWishIcon(state: Boolean) {
        if (state)
            bind.wish.setIconResource(R.drawable.wished)
        else
            bind.wish.setIconResource(R.drawable.wish_24)
    }

    private fun updateUI(product: Product) {
        bind.title.text = product.title
        bind.price.text = "${product.price} ${getString(R.string.pound)}"
        bind.productPics.adapter = ProductPicturesAdapter(product.productPictureUrls)

        setupListener()
    }

    private fun setupListener() {
        bind.cart.setOnClickListener {
            changeCartState()
        }

        bind.wish.setOnClickListener {
            changeWishState()
        }
    }

    private fun changeWishState() {
        lifecycleScope.launch(Dispatchers.IO) {
            productViewModel.changeWishState()
        }
    }

    private fun changeCartState() {
        lifecycleScope.launch(Dispatchers.IO) {
            productViewModel.changeCartState()
        }
    }
}