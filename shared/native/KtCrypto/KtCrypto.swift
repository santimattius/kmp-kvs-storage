import CryptoKit
import Foundation

@objc(KtCrypto)
public class KtCrypto: NSObject{

    @objc public func encrypt(input: Data, key: String) -> Data {
        do {
            let keyData = Data(key.data(using: .utf8)!.prefix(32))
            let key = SymmetricKey(data: keyData)
            let sealed = try AES.GCM.seal(input, using: key)
            return try sealed.combined.tryUnwrap()
        } catch {
            return input
        }
    }

    @objc public func decrypt(input: Data, key: String) -> Data {
        do {
            let keyData = Data(key.data(using: .utf8)!.prefix(32))
            let key = SymmetricKey(data: keyData)
            let box = try AES.GCM.SealedBox(combined: input)
            let opened = try AES.GCM.open(box, using: key)
            return opened
        } catch {
            return input
        }
    }
}

public extension Optional {
    func tryUnwrap() throws -> Wrapped {
        if let value = self {
            return value
        } else {
            throw NSError(domain: "", code: 0)
        }
    }
}