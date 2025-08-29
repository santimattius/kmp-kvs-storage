import SwiftUI
import KvsStorage

struct ContentView: View {
    private let kvs = Storage.shared.kvs(name: "user_preferences")
    
    @State private var isDarkModeEnabled = false
    
    var body: some View {
        VStack {
            VStack(spacing: 16) {
                Image(systemName: "swift")
                    .font(.system(size: 80))
                    .foregroundColor(.accentColor)
                Text("Kvs Storage")
            }
            Toggle(isOn: $isDarkModeEnabled) {
                 Text("Is Dark Mode Enabled")
            }.onChange(of: isDarkModeEnabled){
                updateValue(value: isDarkModeEnabled)
            }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .top)
        .padding()
        .background(isDarkModeEnabled ? Color.black: Color.white)
        .environment(\.colorScheme, isDarkModeEnabled ? .dark : .light)
        .task {
            self.isDarkModeEnabled = try! await kvs.getBoolean(
                key: "is_dark_mode_enabled", defValue: false
            ).boolValue
        }
    }
    
    func updateValue(value:Bool)  {
        Task{
            try! await kvs.edit()
                .putBoolean(key: "is_dark_mode_enabled", value: value)
                .commit()
        }
        
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}
