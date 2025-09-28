import CryptoKit
import Foundation

@objc(KtCrypto)
public class KtCrypto: NSObject{

    @objc public func encrypt(input: Data, key: String) -> Data {
        do {
            let symmetricKey = makeKey(from: key)
            print("Key bytes Encrypt:", [UInt8](symmetricKey.withUnsafeBytes { Data($0) }))
            let sealed = try AES.GCM.seal(input, using: symmetricKey)
            guard let combined = sealed.combined else {
                throw NSError(domain: "CryptoError", code: -1, userInfo: [NSLocalizedDescriptionKey: "Failed to combine sealed box"])
            }
            return combined
        } catch {
            print("Encryption error: \(error)")
            return input
        }
    }

    @objc public func decrypt(input: Data, key: String) -> Data {
        do {
            let symmetricKey = makeKey(from: key)
            print("Key bytes Decrypt:", [UInt8](symmetricKey.withUnsafeBytes { Data($0) }))
            let box = try AES.GCM.SealedBox(combined: input)
            return try AES.GCM.open(box, using: symmetricKey)
        } catch {
            print("Decryption error: \(error)")
            return input
        }
    }

    // Derive a 256-bit SymmetricKey from a String
    private func makeKey(from string: String) -> SymmetricKey {
        let hash = SHA256.hash(data: Data(string.utf8))
        return SymmetricKey(data: hash)
    }

}