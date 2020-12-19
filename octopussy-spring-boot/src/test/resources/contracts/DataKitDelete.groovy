import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description 'must delete data kit'
    request {
        method DELETE()
        url '/dataKits/db1000'
    }
    response {
        status NO_CONTENT()
        body ''
    }
}
