import os
import numpy as np
import pandas as pd



def validate_data_length(data, window_size, min_length=50):
   
    return len(data) >= window_size + min_length

def adjust_window_size(data_length, max_window_size):
    
    return min(data_length // 2, max_window_size)

def generate_time_labels(start_year, start_quarter, total_periods, output_mode):
   
    labels = []
    current_year = start_year
    current_quarter = start_quarter

    for _ in range(total_periods):
        if output_mode == "quarter":
            labels.append(f"{current_year}年 Q{current_quarter}")
            current_quarter += 1
            if current_quarter > 4:
                current_quarter = 1
                current_year += 1
        elif output_mode == "month":
            labels.append(f"{current_year}年 {current_quarter}月")
            current_quarter += 1
            if current_quarter > 12:
                current_quarter = 1
                current_year += 1
    return labels

def calculate_transition_matrix(data, flat_range, window_size):
   
    states = []
    for i in range(len(data) - window_size):
        change = (data[i + window_size] - data[i]) / data[i]
        if change > flat_range:
            states.append("Up")
        elif change < -flat_range:
            states.append("Down")
        else:
            states.append("Flat")

    if len(states) < 2:
        print("Error: Not enough states for transition matrix calculation.")
        return None, None

    unique_states = list(set(states))
    transition_matrix = pd.DataFrame(0, index=unique_states, columns=unique_states)

    for i in range(len(states) - 1):
        transition_matrix.loc[states[i], states[i + 1]] += 1

    transition_matrix = transition_matrix.div(transition_matrix.sum(axis=1), axis=0).fillna(0)
    return transition_matrix, states[-1]

def markov_forecast(file_path, flat_range=0.02, window_size=24, target_year=130, output_mode="quarter"):
    data = pd.read_csv(file_path)['Price'].values

    
    if not validate_data_length(data, window_size):
        print(f"Error: Not enough data in {file_path} to support window_size={window_size}. Skipping...")
        return [], [], np.nan

  
    window_size = adjust_window_size(len(data), window_size)

   
    train_size = int(len(data) * 0.7)
    train_data = data[:train_size]
    test_data = data[train_size:]

   
    transition_matrix, last_state = calculate_transition_matrix(train_data, flat_range, window_size)
    if transition_matrix is None:
        return [], [], np.nan

    
    predictions = []
    current_price = train_data[-1]
    total_periods = (target_year - 113) * 4 if output_mode == "quarter" else (target_year - 113) * 12

    for _ in range(total_periods):
        next_state_prob = transition_matrix.loc[last_state]
        if next_state_prob.sum() == 0:
            next_state = "Flat"
        else:
            next_state = np.random.choice(next_state_prob.index, p=next_state_prob.values)

        change = {
            "Up": np.random.uniform(flat_range, flat_range * 2),
            "Down": np.random.uniform(-flat_range * 2, -flat_range),
            "Flat": np.random.uniform(-flat_range, flat_range)
        }[next_state]
        predicted_price = current_price * (1 + change)
        predictions.append(predicted_price)
        current_price = predicted_price
        last_state = next_state

   
    time_labels = generate_time_labels(113, 1, total_periods, output_mode)

   
    if len(test_data) <= window_size:
        print(f"Warning: Test data is not sufficient for accuracy calculation.")
        return time_labels, predictions, np.nan

    test_predictions = []
    for i in range(len(test_data) - window_size):
        change = (test_data[i + window_size] - test_data[i]) / test_data[i]
        predicted_change = {
            "Up": flat_range * 1.5,
            "Down": -flat_range * 1.5,
            "Flat": 0
        }[last_state]
        test_predictions.append(test_data[i] * (1 + predicted_change))

    test_accuracy = np.mean(1 - np.abs(np.array(test_predictions) - test_data[window_size:]) / test_data[window_size:])
    return time_labels, predictions, test_accuracy

def process_all_districts_markov(base_path, flat_range=0.02, window_size=24, target_year=130, output_mode="quarter"):
    results = []
    for city in os.listdir(base_path):
        city_path = os.path.join(base_path, city)
        if os.path.isdir(city_path):
            for file in os.listdir(city_path):
                if file.endswith("_random_monthly.csv"):
                    file_path = os.path.join(city_path, file)
                    district_name = file.replace("_random_monthly.csv", "")
                    time_labels, predictions, accuracy = markov_forecast(
                        file_path, flat_range, window_size, target_year, output_mode
                    )

                  
                    if predictions:
                        save_path = f"predictions_markov/{city}/{district_name}"
                        os.makedirs(save_path, exist_ok=True)
                        result_df = pd.DataFrame({
                            "Time": time_labels,
                            "Predicted Price": predictions
                        })
                        result_df.to_csv(f"{save_path}/predictions_{output_mode}.csv", index=False)
                        print(f"Markov predictions for {city} - {district_name} saved to '{save_path}/predictions_{output_mode}.csv'")
                        print(f"Accuracy for {city} - {district_name}: {accuracy:.2%}")
                        results.append({"City": city, "District": district_name, "Accuracy": accuracy})

    results_df = pd.DataFrame(results)
    results_df.to_csv(f"markov_overall_accuracy_{output_mode}.csv", index=False)
    print(f"Overall accuracy saved to 'markov_overall_accuracy_{output_mode}.csv'")

if __name__ == "__main__":
    base_path = "data_" 
    target_year = 120 
    flat_range = 0.05  
    window_size = 3  
    output_mode = "quarter"  

    process_all_districts_markov(base_path, flat_range, window_size, target_year, output_mode)