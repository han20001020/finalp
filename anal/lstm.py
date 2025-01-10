import os
import numpy as np
import pandas as pd
from sklearn.preprocessing import MinMaxScaler
import tensorflow as tf


def generate_time_labels(start_year, start_quarter, total_periods, output_mode="quarter"):
    labels = []
    year = start_year
    quarter = start_quarter
    month = (start_quarter - 1) * 3 + 1

    for _ in range(total_periods):
        if output_mode == "quarter":
            labels.append(f"{year}年 Q{quarter}")
            quarter += 1
            if quarter > 4:
                quarter = 1
                year += 1
        elif output_mode == "month":
            labels.append(f"{year}年 {month}月")
            month += 1
            if month > 12:
                month = 1
                year += 1
    return labels


def dynamic_forecast(file_path, n_steps=3, epochs=50, batch_size=32, target_year=130, output_mode="quarter"):
    data = pd.read_csv(file_path)['Price'].values

    
    scaler = MinMaxScaler()
    scaled_data = scaler.fit_transform(data.reshape(-1, 1))

    # 準備 LSTM 輸入
    X, y = [], []
    for i in range(n_steps, len(scaled_data)):
        X.append(scaled_data[i - n_steps:i, 0])
        y.append(scaled_data[i, 0])
    X, y = np.array(X), np.array(y)

    # 進行訓練測試分割
    train_size = int(len(X) * 0.8)
    X_train, y_train = X[:train_size], y[:train_size]
    X_test, y_test = X[train_size:], y[train_size:]

    # 調整形狀以符合 LSTM 的輸入要求
    X_train = X_train.reshape((X_train.shape[0], X_train.shape[1], 1))
    X_test = X_test.reshape((X_test.shape[0], X_test.shape[1], 1))

    # 構建 LSTM 模型
    model = tf.keras.Sequential([
        tf.keras.layers.LSTM(50, return_sequences=True, input_shape=(n_steps, 1)),
        tf.keras.layers.LSTM(50, return_sequences=False),
        tf.keras.layers.Dense(1)
    ])
    model.compile(optimizer='adam', loss='mean_squared_error')

    # 模型訓練
    model.fit(X_train, y_train, epochs=epochs, batch_size=batch_size, verbose=1)

    # 預測測試集準確率
    predictions = model.predict(X_test)
    test_predictions = scaler.inverse_transform(predictions)
    y_test_actual = scaler.inverse_transform(y_test.reshape(-1, 1))
    accuracy = np.mean(1 - np.abs(test_predictions - y_test_actual) / y_test_actual)

    # 動態預測
    def forecast(history, steps):
        predictions = []
        for _ in range(steps):
            # 檢查輸入形狀是否正確
            input_data = np.array(history[-n_steps:]).reshape(1, n_steps, 1)
            predicted = model.predict(input_data, verbose=0)
            predictions.append(predicted[0, 0])
            history.append(predicted[0, 0])
        return predictions

   
    total_periods = (target_year - 113) * 4 if output_mode == "quarter" else (target_year - 113) * 12
    forecast_scaled = forecast(list(scaled_data[-n_steps:, 0]), total_periods)

   
    forecast = scaler.inverse_transform(np.array(forecast_scaled).reshape(-1, 1))
    time_labels = generate_time_labels(113, 1, total_periods, output_mode)

    return time_labels, forecast.flatten(), accuracy



def process_all_districts_lstm(base_path, n_steps=3, epochs=50, batch_size=32, target_year=130, output_mode="quarter"):
    results = []
    for city in os.listdir(base_path):
        city_path = os.path.join(base_path, city)
        if os.path.isdir(city_path):
            for file in os.listdir(city_path):
                if file.endswith("_random_monthly.csv"):
                    file_path = os.path.join(city_path, file)
                    district_name = file.replace("_random_monthly.csv", "")
                    time_labels, predictions, accuracy = dynamic_forecast(
                        file_path, n_steps, epochs, batch_size, target_year, output_mode
                    )

                 
                    save_path = f"predictions_lstm/{city}/{district_name}"
                    os.makedirs(save_path, exist_ok=True)
                    result_df = pd.DataFrame({
                        "Time": time_labels,
                        "Predicted Price": predictions
                    })
                    result_df.to_csv(f"{save_path}/predictions_{output_mode}.csv", index=False)
                    print(f"LSTM predictions for {city} - {district_name} saved to '{save_path}/predictions_{output_mode}.csv'")
                    print(f"Accuracy for {city} - {district_name}: {accuracy:.2%}")
                    results.append({"City": city, "District": district_name, "Accuracy": accuracy})
    results_df = pd.DataFrame(results)
    results_df.to_csv(f"lstm_overall_accuracy_{output_mode}.csv", index=False)
    print(f"Overall accuracy saved to 'lstm_overall_accuracy_{output_mode}.csv'")


if __name__ == "__main__":
    base_path = "data"  
    target_year = 120  
    output_mode = "quarter"  
    process_all_districts_lstm(base_path, target_year=target_year, output_mode=output_mode)
