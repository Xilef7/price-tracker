const query = `fragment ProductMedia on pdpDataProductMedia {
  media {
    type
    urlOriginal: URLOriginal
    urlThumbnail: URLThumbnail
    urlMaxRes: URLMaxRes
    videoUrl: videoURLAndroid
    prefix
    suffix
    description
    variantOptionID
    __typename
  }
  videos {
    source
    url
    __typename
  }
  __typename
}

fragment ProductHighlight on pdpDataProductContent {
  name
  price {
    value
    currency
    priceFmt
    slashPriceFmt
    discPercentage
    __typename
  }
  campaign {
    campaignID
    campaignType
    campaignTypeName
    campaignIdentifier
    background
    percentageAmount
    originalPrice
    discountedPrice
    originalStock
    stock
    stockSoldPercentage
    threshold
    startDate
    endDate
    endDateUnix
    appLinks
    isAppsOnly
    isActive
    hideGimmick
    showStockBar
    __typename
  }
  thematicCampaign {
    additionalInfo
    background
    campaignName
    icon
    __typename
  }
  stock {
    useStock
    value
    stockWording
    __typename
  }
  variant {
    isVariant
    parentID
    __typename
  }
  wholesale {
    minQty
    price {
      value
      currency
      __typename
    }
    __typename
  }
  isCashback {
    percentage
    __typename
  }
  isTradeIn
  isOS
  isPowerMerchant
  isWishlist
  isCOD
  preorder {
    duration
    timeUnit
    isActive
    preorderInDays
    __typename
  }
  __typename
}

fragment ProductInfo on pdpDataProductInfo {
  row
  content {
    title
    subtitle
    applink
    __typename
  }
  __typename
}

fragment ProductDetail on pdpDataProductDetail {
  title
  productDetailDescription {
    title
    content
    __typename
  }
  content {
    title
    subtitle
    applink
    showAtFront
    isAnnotation
    __typename
  }
  __typename
}

fragment ProductSocial on pdpDataSocialProof {
  row
  content {
    icon
    title
    subtitle
    applink
    type
    rating
    __typename
  }
  __typename
}

fragment ProductDataInfo on pdpDataInfo {
  icon
  title
  isApplink
  applink
  content {
    icon
    text
    __typename
  }
  __typename
}

fragment ProductCustomInfo on pdpDataCustomInfo {
  icon
  title
  isApplink
  applink
  separator
  description
  __typename
}

fragment ProductVariant on pdpDataProductVariant {
  errorCode
  parentID
  defaultChild
  sizeChart
  totalStockFmt
  variants {
    productVariantID
    variantID
    name
    identifier
    option {
      picture {
        urlOriginal: url
        urlThumbnail: url100
        __typename
      }
      productVariantOptionID
      variantUnitValueID
      value
      hex
      stock
      __typename
    }
    __typename
  }
  children {
    productID
    price
    priceFmt
    slashPriceFmt
    discPercentage
    optionID
    optionName
    productName
    productURL
    picture {
      urlOriginal: url
      urlThumbnail: url100
      __typename
    }
    stock {
      stock
      isBuyable
      stockWordingHTML
      minimumOrder
      maximumOrder
      __typename
    }
    isCOD
    isWishlist
    campaignInfo {
      campaignID
      campaignType
      campaignTypeName
      campaignIdentifier
      background
      discountPercentage
      originalPrice
      discountPrice
      stock
      stockSoldPercentage
      startDate
      endDate
      endDateUnix
      appLinks
      isAppsOnly
      isActive
      hideGimmick
      isCheckImei
      minOrder
      showStockBar
      __typename
    }
    thematicCampaign {
      additionalInfo
      background
      campaignName
      icon
      __typename
    }
    ttsPID
    ttsSKUID
    __typename
  }
  __typename
}

fragment ProductCategoryCarousel on pdpDataCategoryCarousel {
  linkText
  titleCarousel
  applink
  list {
    categoryID
    icon
    title
    isApplink
    applink
    __typename
  }
  __typename
}

fragment ProductDetailMediaComponent on pdpDataProductDetailMediaComponent {
  title
  description
  contentMedia {
    url
    ratio
    type
    __typename
  }
  show
  ctaText
  __typename
}

fragment PdpDataComponentShipmentV4 on pdpDataComponentShipmentV4 {
  data {
    productID
    warehouse_info {
      warehouse_id
      is_fulfillment
      district_id
      postal_code
      geolocation
      city_name
      ttsWarehouseID
      __typename
    }
    useBOVoucher
    isCOD
    metadata
    __typename
  }
  __typename
}

query PDPMainInfo($productKey: String, $shopDomain: String, $layoutID: String, $extraPayload: String, $queryParam: String, $source: String, $userLocation: pdpUserLocation) {
  pdpMainInfo(shopDomain: $shopDomain, productKey: $productKey, layoutID: $layoutID, extraPayload: $extraPayload, queryParam: $queryParam, source: $source, userLocation: $userLocation) {
    requestID
    extraPayload
    data {
      layoutName
      basicInfo {
        alias
        createdAt
        isQA
        id: productID
        shopID
        shopName
        minOrder
        maxOrder
        weight
        weightUnit
        condition
        status
        url
        needPrescription
        catalogID
        isLeasing
        isBlacklisted
        isTokoNow
        defaultMediaURL
        menu {
          id
          name
          url
          __typename
        }
        blacklistMessage {
          identifier
          imageURL
          title
          description
          button
          buttonArea
          buttonName
          url
          supportingImage {
            url
            width
            height
            __typename
          }
          __typename
        }
        category {
          id
          name
          title
          breadcrumbURL
          isAdult
          isKyc
          minAge
          detail {
            id
            name
            breadcrumbURL
            isAdult
            __typename
          }
          ttsID
          ttsDetail {
            id
            name
            breadcrumbURL
            isAdult
            __typename
          }
          __typename
        }
        txStats {
          transactionSuccess
          transactionReject
          countSold
          paymentVerified
          itemSoldFmt
          __typename
        }
        stats {
          countView
          countReview
          countTalk
          rating
          __typename
        }
        productID
        ttsPID
        ttsSKUID
        ttsShopID
        isAggregatedWithTTS
        __typename
      }
      __typename
    }
    components {
      name
      type
      kind
      position
      data {
        ...ProductMedia
        ...ProductHighlight
        ...ProductInfo
        ...ProductDetail
        ...ProductSocial
        ...ProductDataInfo
        ...ProductCustomInfo
        ...ProductVariant
        ...ProductCategoryCarousel
        ...ProductDetailMediaComponent
        ...PdpDataComponentShipmentV4
        __typename
      }
      __typename
    }
    __typename
  }
}`;

const reservedFirstPathSegment = [
    "promo",
    "digital",
    "mybills",
    "login",
    "official-store",
    "kereta-api",
    "berbagi",
    "contact-us",
    "kartu-kredit",
    "mpn",
    "pasang-tv-kabel",
    "pendidikan",
    "roaming",
    "tv-kabel",
    "retribusi",
    "tagihan",
    "donasi-online",
    "pajak",
    "paket-data",
    "pln",
    "pulsa",
    "angsuran",
    "belajar",
    "donasi",
    "gift-card",
    "top-up",
    "voucher-game",
    "biaya-pendidikan",
    "kartu-prakerja",
    "streaming",
    "contact-us.pl",
    "bantuan",
    "p",
    "mobile-apps",
    "about",
    "edu",
    "help",
    "discovery",
    "rewards",
    "top-up-tagihan",
    "partner",
    "catalog",
    "flight",
    "hotel",
    "deals",
    "pinjaman-online",
    "reksa-dana",
    "asuransi",
    "emas",
    "s",
    "find",
    "blog",
    "events",
    "panduan-keamanan",
    "perlindungan-kekayaan-intelektual",
    "careers",
    "daftar-halaman",
    "cod",
    "terms",
    "privacy",
    "index.php",
    "Profile",
    "tokopedia",
    "store",
    "id"
];
const reservedSecondPathSegment = ['product', 'review', 'etalase'];

async function getAndStorePrices() {
    const [, shopDomain, productKey] = window.location.pathname.split('/', 3);

    if (!shopDomain || !productKey) return;
    if (reservedFirstPathSegment.includes(shopDomain) || reservedSecondPathSegment.includes(productKey)) return;

    const response = await fetch("https://gql.tokopedia.com/graphql/PDPMainInfo", {
        headers: {
            "content-type": "application/json",
            "x-tkpd-akamai": "pdpMainInfo",
        },
        body: JSON.stringify(
            [
                {
                    operationName: "PDPMainInfo",
                    variables: {
                        productKey,
                        shopDomain,
                        "layoutID": "",
                        "extraPayload": "",
                        "queryParam": "",
                        "source": "P1",
                        "userLocation": {
                            "addressID": "",
                            "districtID": "2274",
                            "postalCode": "",
                            "latlon": "",
                            "cityID": "176"
                        }
                    },
                    query
                }
            ]
        ),
        "method": "POST",
    });
    const now = Date.now();
    const instant = now - now % (60 * 60 * 1000); // truncated to hour

    const [{ data: { pdpMainInfo }, errors }] = await response.json();
    if (errors.length > 0) {
        console.debug('pdpMainInfo: ', { shopDomain, productKey });
    }
    const pdpComponents = pdpMainInfo.components;
    const variant = pdpComponents.find(({ type, data }) => type === "variant" && data.length);

    const message = variant
        ? variant.data[0].children.map(({ ttsSKUID, price }) => ({ skuId: ttsSKUID, instant, price }))
        : [{
            skuId: pdpMainInfo.data.basicInfo.ttsSKUID,
            instant,
            price: pdpComponents.find(({ name }) => name === "product_content").data[0].price.value,
        }];

    const newResult = {};
    for (const { skuId, instant, price } of message) {
        newResult[`${skuId}.${instant}`] = { min: price, max: price, initial: price};
    }

    const result = await chrome.storage.local.get(newResult);

    for (const [key, statistic] of Object.entries(result)) {
        const { initial: price } = newResult[key];
        const { min, max } = statistic;
        if (price < min) {
            statistic.min = price;
            statistic.sent = false;
        }
        if (price > max) {
            statistic.max = price;
            statistic.sent = false;
        }
    }

    await chrome.storage.local.set(result);
}

window.addEventListener("pushstate", () => {
    getAndStorePrices();
});

getAndStorePrices();
